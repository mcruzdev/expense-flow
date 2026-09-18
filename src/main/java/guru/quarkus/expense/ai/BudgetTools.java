package guru.quarkus.expense.ai;

import dev.langchain4j.agent.tool.Tool;
import guru.quarkus.expense.domain.Budget;
import guru.quarkus.expense.domain.BudgetContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;

@ApplicationScoped
public class BudgetTools {

    @ActivateRequestContext
    @Tool(name = "getCurrentBudget", value = "Returns the company's current budget, including the total allocated amount and how much has already been spent")
    public BudgetContext getCurrentBudget() {
        Budget budget = Budget.current();
        if (budget == null) {
            throw new IllegalStateException("No budget is configured");
        }
        return new BudgetContext(budget.totalBudget, budget.spent);
    }
}
