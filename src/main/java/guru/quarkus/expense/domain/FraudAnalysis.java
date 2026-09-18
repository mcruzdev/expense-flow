package guru.quarkus.expense.domain;

import java.util.List;

public record FraudAnalysis(
        RiskLevel riskLevel,
        double confidence,
        List<String> findings,
        boolean requiresHumanReview
) {
}
