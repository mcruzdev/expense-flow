package guru.quarkus.expense.domain;

public record ReviewerDecisionEvent(Long expenseID, Decision Decision) {
}
