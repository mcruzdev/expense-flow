package guru.quarkus.expense.data;

import java.math.BigDecimal;

public record SubmitExpenseRequest(String attachmentLocation, BigDecimal amount, String description) {
}
