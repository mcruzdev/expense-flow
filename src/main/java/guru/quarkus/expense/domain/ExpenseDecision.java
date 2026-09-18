package guru.quarkus.expense.domain;

import java.time.Instant;
import java.util.List;

public record ExpenseDecision(
        Decision decision,
        String explanation,
        List<String> reasons,
        List<String> requiredHumanActions,
        Instant decidedAt
) {

    public static ExpenseDecision byReviewer(Decision decision, String explanation) {
        return new ExpenseDecision(decision, explanation, List.of(), List.of(), Instant.now());
    }
}
