package es.ucm.fdi.iw.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.User;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user/notifications")
public class NotificationsController {

    private static final Logger log = LogManager.getLogger(NotificationsController.class);

    @Autowired
    private EntityManager entityManager;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @GetMapping
    @Transactional
    public String notifications(Model model, HttpSession session) {
        User u = (User) session.getAttribute("u");
        if (u == null) {
            return "redirect:/login";
        }

        List<Notification> unreadByUser = entityManager
                .createNamedQuery("Notification.getUnreadByUser", Notification.class)
                .setParameter("uid", u.getId())
                .getResultList();

        model.addAttribute("notifications", unreadByUser);

        // Marcar todas como leídas
        for (Notification n : unreadByUser) {
            n.setReadByUser(true);
            entityManager.merge(n);
        }

        return "notifications";
    }

    /**
     * Marca una notificación como leída.
     * Se llama desde JS cuando llega una notificación por WebSocket
     * y el usuario está en la página de notificaciones.
     */
    @PostMapping(path = "/{id}/read", produces = "application/json")
    @Transactional
    public ResponseEntity<String> markAsRead(@PathVariable Long id, HttpSession session) {
        User u = (User) session.getAttribute("u");
        if (u == null) {
            return ResponseEntity.status(403).body("{\"error\": \"No autenticado\"}");
        }

        Notification notif = entityManager.find(Notification.class, id);
        if (notif == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Notificación no encontrada\"}");
        }

        // Verificar que la notificación pertenece al usuario
        if (notif.getUser().getId() != u.getId()) {
            return ResponseEntity.status(403).body("{\"error\": \"No autorizado\"}");
        }

        notif.setReadByUser(true);
        entityManager.merge(notif);

        return ResponseEntity.ok("{\"ok\": true}");
    }
}
