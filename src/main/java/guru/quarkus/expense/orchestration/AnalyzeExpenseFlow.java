package guru.quarkus.expense.orchestration;

import dev.langchain4j.data.message.ImageContent;
import guru.quarkus.expense.ai.Planner;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.Decision;
import guru.quarkus.expense.domain.Expense;
import guru.quarkus.expense.domain.ExpenseDecision;
import guru.quarkus.expense.domain.ReviewerDecisionEvent;
import guru.quarkus.expense.infra.AmazonS3Service;
import io.quarkiverse.flow.Flow;
import io.quarkiverse.flow.dsl.FlowWorkflowBuilder;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.serverlessworkflow.api.types.Workflow;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URL;
import java.util.Map;

import static io.quarkiverse.flow.dsl.FlowDSL.consumed;
import static io.quarkiverse.flow.dsl.FlowDSL.function;
import static io.quarkiverse.flow.dsl.FlowDSL.listen;
import static io.quarkiverse.flow.dsl.FlowDSL.on;
import static io.quarkiverse.flow.dsl.FlowDSL.one;
import static io.quarkiverse.flow.dsl.FlowDSL.switchWhenOrElse;
import static io.quarkiverse.flow.dsl.FlowDSL.toOne;

@ApplicationScoped
public class AnalyzeExpenseFlow extends Flow {

    final Planner planner;
    final AmazonS3Service amazonS3Service;

    public AnalyzeExpenseFlow(Planner planner,
                              AmazonS3Service amazonS3Service) {
        this.planner = planner;
        this.amazonS3Service = amazonS3Service;
    }

    @Override
    public Workflow descriptor() {
        return FlowWorkflowBuilder
                .workflow("analyzeExpense", "expenseFlow", "0.1.0")
                .schedule(on(one(AnalysisRequestedEvent.CE_TYPE)))
                .inputFrom(".[0].data")
                .tasks(

                        function("analyzeByAgents", (AnalysisRequestedEvent e) -> {
                            URL url = amazonS3Service.preSignedGetURL(e.attachmentLocation());
                            ImageContent receiptImage = ImageContent.from(url.toString(), ImageContent.DetailLevel.HIGH);
                            ExpenseDecision decision = planner.analyze(receiptImage, e);
                            return new AnalysisResult(e.expenseID(), decision);
                        }),
                        function("handleLlmDecision", (AnalysisResult result) -> {
                            Log.infov("Final decision is: {0}", result.decision());
                            QuarkusTransaction.requiringNew().run(() -> {
                                Expense expense = Expense.findById(result.expenseID());
                                expense.addDecision(result.decision);
                            });
                            return result;
                        }),
                        switchWhenOrElse("route", (AnalysisResult result) ->
                                result.decision.decision() == Decision.REVIEW, "waitHuman", "proceed"),
                        listen("waitHuman", toOne(consumed("guru.quarkus.expense.human.decision"))),
                        function("handleHumanDecision", (ReviewerDecisionEvent event) -> {
                            QuarkusTransaction.requiringNew().run(() -> Expense.update("status = :status where id = :id",
                                    Map.of("status", Expense.Status.AI_REVIEWED, "id", event.expenseID())));
                            return event;
                        })

                )
                .build();
    }

    private record AnalysisResult(Long expenseID, ExpenseDecision decision) {
    }
}
