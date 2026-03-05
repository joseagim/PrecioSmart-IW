package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notificacion")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Request request;


}