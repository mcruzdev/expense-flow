package guru.quarkus.expense.resource;

import guru.quarkus.expense.data.HumanDecisionRequest;
import guru.quarkus.expense.data.PreSignedURLRequest;
import guru.quarkus.expense.data.PreSignedURLResponse;
import guru.quarkus.expense.data.SubmitExpenseRequest;
import guru.quarkus.expense.domain.Expense;
import guru.quarkus.expense.domain.ExpenseDecision;
import guru.quarkus.expense.infra.AmazonS3Service;
import guru.quarkus.expense.orchestration.ProcessExpenseFlow;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/* For simplicity this class will handle all HTTP requests. */
@Path("/api")
public class GlobalResource {

    private static final Set<String> ALLOWED_RECEIPT_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/webp", "application/pdf"
    );

    final ProcessExpenseFlow processExpenseFlow;
    final AmazonS3Service amazonS3Service;

    public GlobalResource(ProcessExpenseFlow processExpenseFlow, AmazonS3Service amazonS3Service) {
        this.processExpenseFlow = processExpenseFlow;
        this.amazonS3Service = amazonS3Service;
    }

    @POST
    @Path("/receipts")
    public Response requestPreSignedURL(PreSignedURLRequest request) {
        if (request == null || !ALLOWED_RECEIPT_CONTENT_TYPES.contains(request.contentType())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Unsupported content type. Allowed: " + ALLOWED_RECEIPT_CONTENT_TYPES))
                    .build();
        }
        String attachmentLocation = UUID.randomUUID().toString();
        return Response.accepted(
                new PreSignedURLResponse(amazonS3Service.preSignedPutURL(attachmentLocation, request.contentType()), attachmentLocation)
        ).build();
    }

    @POST
    @Path("/expenses")
    public Response submitExpenses(SubmitExpenseRequest request) {
        boolean uploaded = amazonS3Service.verifyIfUploaded(request.attachmentLocation());
        if (!uploaded) {
            return Response.status(Response.Status.PRECONDITION_FAILED)
                    .entity(Map.of("error", "Receipt attachmentLocation not found. Please upload the file before submitting."))
                    .build();
        }
        processExpenseFlow.startInstance(request)
                .subscribe()
                .with(
                        workflowModel -> Log.infov("GeneratePreSignedObjectFlow executed successfully."),
                        failure -> Log.errorv("GeneratePreSignedObjectFlow failed: {0}", failure.getMessage())
                );
        return Response.accepted(Map.of("attachmentLocation", request.attachmentLocation())).build();
    }

    @PATCH
    @Path("/expenses/{expenseId}")
    public Response humanDecision(@PathParam("expenseId") Long expenseId, HumanDecisionRequest request) {
        if (request == null || request.decision() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "decision is required: APPROVE or REJECT"))
                    .build();
        }

        return QuarkusTransaction.requiringNew().call(() -> {
            Expense expense = Expense.findById(expenseId);
            if (expense == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Expense not found."))
                        .build();
            }

            expense.addReviewerDecision(new ExpenseDecision(request.decision(), request.explanation(), List.of(), List.of(), Instant.now()));
            return Response.ok(expense).build();
        });
    }

    @GET
    public Response expenses() {
        return Response.ok(Expense.listAll(Sort.by("createdAt"))).build();
    }

    @GET
    @Path("/expenses/{expenseID}/image")
    public Response receiptUrl(@PathParam("expenseID") Long expenseId) {
        Expense expense = Expense.findById(expenseId);
        if (expense == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Expense not found."))
                    .build();
        }
        if (expense.receipt == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No receipt attached to this expense."))
                    .build();
        }
        return Response.ok(Map.of("url", amazonS3Service.preSignedGetURL(expense.receipt.attachmentLocation).toString())).build();
    }
}
