package guru.quarkus.expense.domain;

public record FinalAnalysis(
        PolicyAnalysis policyAnalysis, FraudAnalysis fraudAnalysis, BudgetAnalysis budgetAnalysis) {
}
