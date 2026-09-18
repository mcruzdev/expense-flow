package guru.quarkus.expense.orchestration;

import guru.quarkus.expense.data.SubmitExpenseRequest;
import guru.quarkus.expense.domain.Expense;
import guru.quarkus.expense.domain.Receipt;
import io.quarkiverse.flow.Flow;
import io.quarkiverse.flow.dsl.FlowWorkflowBuilder;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.serverlessworkflow.api.types.Workflow;
import jakarta.enterprise.context.ApplicationScoped;

import static io.quarkiverse.flow.dsl.FlowDSL.function;

@ApplicationScoped
public class ProcessExpenseFlow extends Flow {

    @Override
    public Workflow descriptor() {
        return FlowWorkflowBuilder
                .workflow("expenseFlow", "processReceiptFlow", "1.0.0")
                .tasks(
                        function("persist", (SubmitExpenseRequest in) -> QuarkusTransaction.requiringNew()
                                .call(() -> {
                                    Receipt receipt = new Receipt(in.attachmentLocation());
                                    receipt.persist();

                                    Expense expense = new Expense(receipt, in.amount(), in.description());
                                    expense.persist();

                                    return expense.id;
                                })))
                .build();
    }
}
