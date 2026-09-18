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
}
