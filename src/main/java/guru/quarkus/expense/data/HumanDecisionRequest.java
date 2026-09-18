package guru.quarkus.expense.data;

import guru.quarkus.expense.domain.Decision;

public record HumanDecisionRequest(Decision decision, String explanation) {
}
