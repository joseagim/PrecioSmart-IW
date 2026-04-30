package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notificacion")
@NamedQueries({
    @NamedQuery(name = "Notification.findByUser",
        query = "SELECT n FROM Notification n WHERE n.user.id = :uid ORDER BY n.date DESC"),
    @NamedQuery(name = "Notification.getUnreadByUser",
        query = "SELECT n FROM Notification n WHERE n.user.id = :uid AND n.readByUser = false")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Request request;

    /** Whether the user has seen this notification */
    private boolean readByUser = false;

    /** When the notification was created */
    private LocalDateTime date;
}