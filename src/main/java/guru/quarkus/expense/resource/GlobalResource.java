package guru.quarkus.expense.resource;

import guru.quarkus.expense.domain.Expense;
import guru.quarkus.expense.infra.AmazonS3Service;
import guru.quarkus.expense.data.PreSignedURLRequest;
import guru.quarkus.expense.data.PreSignedURLResponse;
import guru.quarkus.expense.data.SubmitExpenseRequest;
import guru.quarkus.expense.orchestration.ProcessExpenseFlow;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

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
                new PreSignedURLResponse(amazonS3Service.preSignedURL(attachmentLocation, request.contentType()), attachmentLocation)
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

    @GET
    public Response expenses() {
        return Response.ok(Expense.listAll(Sort.by("createdAt"))).build();
    }
}
