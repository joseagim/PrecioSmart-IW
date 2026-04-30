package es.ucm.fdi.iw.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
}
