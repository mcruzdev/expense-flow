package guru.quarkus.expense.domain;

import java.math.BigDecimal;

public record BudgetContext(BigDecimal totalBudget, BigDecimal spent) {
}
