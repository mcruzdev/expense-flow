package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.BudgetAnalysis;
import guru.quarkus.expense.domain.ExpenseDecision;
import guru.quarkus.expense.domain.FraudAnalysis;
import guru.quarkus.expense.domain.PolicyAnalysis;
import guru.quarkus.expense.domain.ReceiptAnalysis;

public interface DecisionAgent {

    @UserMessage("""
            You are responsible for making the final recommendation
            for an employee expense.
            
            Expense:
            {{expense}}
            
            Receipt analysis:
            {{receiptAnalysis}}
            
            Policy analysis:
            {{policyAnalysis}}
            
            Fraud analysis:
            {{fraudAnalysis}}
            
            Budget analysis:
            {{budgetAnalysis}}
            
            Use the following decision rules:
            
            APPROVE:
            The available evidence indicates that the expense complies
            with applicable policies and there are no unresolved concerns.
            
            REJECT:
            There is clear evidence of a policy violation.
            
            REVIEW:
            Information is missing, ambiguous, contradictory, or there
            is a significant concern that requires human judgment.
            
            Never invent information.
            
            If you choose REVIEW, explain exactly what the human needs
            to resolve.
            
            Return only the structured decision.
            """)
    @Agent(value = "Makes the final reimbursement recommendation", outputKey = "decision")
    ExpenseDecision decide(
            @V("receiptAnalysis") ReceiptAnalysis receiptAnalysis,
            @V("policyAnalysis") PolicyAnalysis policyAnalysis,
            @V("fraudAnalysis") FraudAnalysis fraudAnalysis,
            @V("budgetAnalysis") BudgetAnalysis budgetAnalysis,
            @V("expense") AnalysisRequestedEvent expense);
}
