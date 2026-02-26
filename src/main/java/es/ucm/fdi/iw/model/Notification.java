package es.ucm.fdi.iw.model;

import jakarta.persistence.*;


@Entity
@Table(name = "notificacion")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Request request;

    @Version
    private Long version;


}