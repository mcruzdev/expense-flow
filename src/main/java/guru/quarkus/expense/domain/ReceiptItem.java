package guru.quarkus.expense.domain;

import java.math.BigDecimal;

public record ReceiptItem(
        String description,
        BigDecimal amount
) {
}
