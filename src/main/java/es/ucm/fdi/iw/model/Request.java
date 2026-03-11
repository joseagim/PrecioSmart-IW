package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String EAN;
    private String info;
    
    private LocalDateTime date;
    
    private float price;
    private boolean approved;

    @ManyToOne
    private User user; // Generará la columna 'user_id' automáticamente
}