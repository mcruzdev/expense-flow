package guru.quarkus.expense.domain;

public record ReviewerDecisionEvent(Long expenseID, ExpenseDecision expenseDecision) {
    public static final String CE_TYPE = "guru.quarkus.expense.reviewer.decision";
}
