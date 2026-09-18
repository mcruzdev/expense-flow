package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.agentic.declarative.ParallelAgent;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.BudgetAnalysis;
import guru.quarkus.expense.domain.FinalAnalysis;
import guru.quarkus.expense.domain.FraudAnalysis;
import guru.quarkus.expense.domain.PolicyAnalysis;
import guru.quarkus.expense.domain.ReceiptAnalysis;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface ParallelAnalyzer {

    @ParallelAgent(subAgents = {
            PolicyAgent.class, FraudAgent.class, BudgetAgent.class
    }, outputKey = "finalAnalysis")
    FinalAnalysis analyze(@V("receiptAnalysis") ReceiptAnalysis receiptAnalysis, @V("expense") AnalysisRequestedEvent expense);

    @Output
    static FinalAnalysis join(
            @V("policyAnalysis") PolicyAnalysis policyAnalysis,
            @V("fraudAnalysis") FraudAnalysis fraudAnalysis,
            @V("budgetAnalysis") BudgetAnalysis budgetAnalysis) {
        return new FinalAnalysis(
                policyAnalysis, fraudAnalysis, budgetAnalysis);
    }

}
