package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@NamedQueries({
        @NamedQuery(name = "findByStatus", query = "SELECT r FROM Request r WHERE r.status = :status ORDER BY r.date DESC")
})
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String EAN;
    private String name;
    private String brand;
    private String quantity;
    private String supermarket;
    private String imageUrl;
    private float price;
    private LocalDateTime date;
    private RequestType type;
    private RequestStatus status;

    @ManyToOne
    private User user; // Generará la columna 'user_id' automáticamente
}