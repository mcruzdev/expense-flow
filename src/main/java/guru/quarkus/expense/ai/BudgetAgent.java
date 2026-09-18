package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.BudgetAnalysis;
import io.quarkiverse.langchain4j.ToolBox;

public interface BudgetAgent {

    @UserMessage("""
            Analyze the following expense against the department budget.
            
            Expense:
            {{expense}}
            
            Current budget:
            Call the getCurrentBudget tool to retrieve the current total budget and the amount already spent.
            
            Determine:
            - current budget utilization
            - remaining budget after this expense
            - whether this expense creates a budget concern
            
            Show the relevant calculations.
            
            Do not approve or reject the expense.
            
            Return only the structured result.
            """)
    @Agent(value = "Analyzes the impact of an expense on the department budget", outputKey = "budgetAnalysis")
    @ToolBox(BudgetTools.class)
    BudgetAnalysis analyze(@V("expense") AnalysisRequestedEvent expense);
}
