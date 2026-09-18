package guru.quarkus.expense.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
public class Budget extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "total_budget", nullable = false)
    public BigDecimal totalBudget;
    @Column(nullable = false)
    public BigDecimal spent;

    protected Budget() {
    }

    public Budget(BigDecimal totalBudget, BigDecimal spent) {
        this.totalBudget = totalBudget;
        this.spent = spent;
    }

    public static Budget current() {
        return findAll().firstResult();
    }
}
