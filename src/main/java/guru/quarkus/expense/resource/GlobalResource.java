package guru.quarkus.expense.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.quarkus.expense.data.HumanDecisionRequest;
import guru.quarkus.expense.data.PreSignedURLRequest;
import guru.quarkus.expense.data.PreSignedURLResponse;
import guru.quarkus.expense.data.SubmitExpenseRequest;
import guru.quarkus.expense.domain.Expense;
import guru.quarkus.expense.domain.ExpenseDecision;
import guru.quarkus.expense.domain.ReviewerDecisionEvent;
import guru.quarkus.expense.infra.AmazonS3Service;
import guru.quarkus.expense.orchestration.ProcessExpenseFlow;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonCloudEventData;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.serverlessworkflow.impl.WorkflowApplication;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.time.OffsetDateTime;
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
    final WorkflowApplication app;
    final ObjectMapper jackson;

    public GlobalResource(ProcessExpenseFlow processExpenseFlow, AmazonS3Service amazonS3Service, WorkflowApplication app, ObjectMapper jackson) {
        this.processExpenseFlow = processExpenseFlow;
        this.amazonS3Service = amazonS3Service;
        this.app = app;
        this.jackson = jackson;
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
    @Path("/expenses/{expenseID}")
    public Response humanDecision(@PathParam("expenseID") Long expenseID, HumanDecisionRequest request) {

        if (request == null || request.decision() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "decision is required: APPROVE or REJECT"))
                    .build();
        }

        ReviewerDecisionEvent event = new ReviewerDecisionEvent(
                expenseID, ExpenseDecision.byReviewer(request.decision(), request.explanation()));

        // It can be done by Microprofile Reactive Messaging channels
        JsonNode data = jackson.convertValue(event, JsonNode.class);
        app.eventPublishers()
                .stream()
                .findAny()
                .ifPresent(publisher -> publisher.publish(
                        CloudEventBuilder.v1()
                                .withId(UUID.randomUUID().toString())
                                .withType(ReviewerDecisionEvent.CE_TYPE)
                                .withTime(OffsetDateTime.now())
                                .withData(JsonCloudEventData.wrap(data))
                                .withSource(URI.create("https://guru.quarkus/expense-flow"))
                                .build()
                ));

        return Response.accepted().build();
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
