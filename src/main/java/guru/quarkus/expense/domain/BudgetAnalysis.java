package guru.quarkus.expense.domain;

import java.math.BigDecimal;

public record BudgetAnalysis(
        BigDecimal budget,
        BigDecimal spent,
        BigDecimal expenseAmount,
        BigDecimal remainingBudget,
        boolean budgetConcern,
        String explanation
) {
}
