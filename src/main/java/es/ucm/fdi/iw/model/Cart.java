package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private LocalDateTime date;

    private float total;

    @OneToMany(mappedBy = "cart")
    private List<ProductCart> items; // lines of the cart

    @ManyToOne
    private User user;

}
