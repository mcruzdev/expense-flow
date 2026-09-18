package guru.quarkus.expense.domain;


import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "receipts")
public class Receipt extends PanacheEntity {

    @Column(name = "attachment_location", nullable = false)
    public String attachmentLocation;

    protected Receipt() {
    }

    public Receipt(String attachmentLocation) {
        this.attachmentLocation = attachmentLocation;
    }
}
