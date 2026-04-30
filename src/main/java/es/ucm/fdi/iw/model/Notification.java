package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@NamedQueries({
        @NamedQuery(
            name = "Notification.findUnread", 
            query = "SELECT n FROM Notification n WHERE n.user.id = :uid AND n.read = false"
        )
})
@Table(name = "notificacion")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Request request;
    
    private boolean read;
    private LocalDateTime date;

}