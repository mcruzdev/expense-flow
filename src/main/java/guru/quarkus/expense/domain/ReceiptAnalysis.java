package guru.quarkus.expense.domain;

import java.util.List;

public record ReceiptAnalysis(
        String merchant,
        String date,
        String total,
        String currency,
        String category,
        String businessPurpose,
        List<ReceiptItem> items,
        double confidence,
        List<String> missingInformation
) {
}
