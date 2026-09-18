package guru.quarkus.expense.domain;

import java.math.BigDecimal;

public record AnalysisRequestedEvent(
        String attachmentLocation,
        Long expenseID,
        String description,
        BigDecimal amount
) {

    public static final String CE_TYPE = "guru.quarkus.expense.analysis.requested";
}
