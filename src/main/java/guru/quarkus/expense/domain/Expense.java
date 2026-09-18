package guru.quarkus.expense.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "expenses")
public class Expense extends PanacheEntity {

    public enum Status {
        SUBMITTED,
        APPROVED,
        REJECTED
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
    public Status status;

}
