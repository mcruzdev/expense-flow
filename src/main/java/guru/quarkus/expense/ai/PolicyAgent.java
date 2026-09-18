package guru.quarkus.expense.ai;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import guru.quarkus.expense.domain.AnalysisRequestedEvent;
import guru.quarkus.expense.domain.PolicyAnalysis;
import guru.quarkus.expense.domain.ReceiptAnalysis;

public interface PolicyAgent {

    @UserMessage("""
            Evaluate the following expense against the company policy.

            Expense:
            {{expense}}

            Receipt analysis:
            {{receiptAnalysis}}

            Company policy (Acme Corp Travel & Expense Policy, effective 2026):
            - Meals: max $75/day domestic, $100/day international. Alcohol is not reimbursable.
            - Lodging: max $250/night domestic, $350/night international. Must book standard/economy room class.
            - Ground transportation: taxis/rideshare allowed up to $60/trip; personal vehicle mileage reimbursed at $0.67/mile.
            - Airfare: economy class only for flights under 6 hours; business class requires VP pre-approval.
            - Client entertainment: max $150/person, requires listing attendees and business purpose.
            - All expenses over $25 require an itemized receipt (a credit card slip alone is not sufficient).
            - Expenses must be submitted within 30 days of the purchase date.
            - Any single expense over $500 requires manager approval prior to purchase.
            - Personal items, fines, and gym/spa charges are never reimbursable.

            For every applicable policy rule:
            - determine whether it is satisfied
            - identify violations
            - identify missing or ambiguous information

            Do not make the final reimbursement decision.

            Return only the structured result.
            """)
    @Agent(value = "Determines whether an expense complies with company policy", outputKey = "policyAnalysis")
    PolicyAnalysis analyze(@V("receiptAnalysis") ReceiptAnalysis receiptAnalysis, @V("expense") AnalysisRequestedEvent expense);
}
