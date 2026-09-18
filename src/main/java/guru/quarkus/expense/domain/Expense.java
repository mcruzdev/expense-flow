package guru.quarkus.expense.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense extends PanacheEntity {

    public enum Status {
        SUBMITTED,
        AI_REVIEWED,
        REQUIRES_HUMAN_REVIEW,
        REJECTED,
        APPROVED
    }

    protected Expense() {
    }

    public Expense(Receipt receipt, BigDecimal amount, String description) {
        this.receipt = receipt;
        this.amount = amount;
        this.description = description;
        this.createdAt = Instant.now();
    }

    @OneToOne
    public Receipt receipt;
    @Column(nullable = false)
    public BigDecimal amount;
    @Column(nullable = false)
    public String description;
    @Column(name = "created_at")
    public Instant createdAt;
    @Enumerated(value = EnumType.STRING)
    public Status status = Status.SUBMITTED;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    public List<ExpenseDecision> decisions;

    public void addDecision(ExpenseDecision expenseDecision) {
        this.decisions = new ArrayList<>(List.of(expenseDecision));
        switch (expenseDecision.decision()) {
            case REJECT -> status = Status.REJECTED;
            case REVIEW -> status = Status.REQUIRES_HUMAN_REVIEW;
            case APPROVE -> status = Status.APPROVED;
        }
    }

    public void addReviewerDecision(ExpenseDecision decision) {
        if (this.decisions == null) {
            this.decisions = new ArrayList<>();
        }
        this.decisions.add(decision);
        switch (decision.decision()) {
            case REJECT -> status = Status.REJECTED;
            case APPROVE -> status = Status.APPROVED;
            case REVIEW -> throw new IllegalStateException(
                    "Reviewer decision for expense #" + id + " must be APPROVE or REJECT, but was REVIEW. "
                            + "REVIEW is only a valid outcome for the AI's initial decision, not a human reviewer's.");
        }
    }

}
