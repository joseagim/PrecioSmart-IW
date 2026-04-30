package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.RequestStatus;
import es.ucm.fdi.iw.model.RequestType;
import es.ucm.fdi.iw.model.Supermarket;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user/notifications")
public class NotificationController {

    @Autowired
    private EntityManager entityManager;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @GetMapping
    public String getNotifications(Model model, HttpSession session) {

        User user = (User) session.getAttribute("u");

        List<Notification> notis = new ArrayList<>();

        notis = entityManager
                .createNamedQuery("Notification.findUnread", Notification.class)
                .setParameter("uid", user.getId())
                .getResultList();

        model.addAttribute("notifications", notis);

        return "notifications";
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, String>> markAsRead(@RequestParam long id, HttpSession session) {

        Notification notification = entityManager.find(Notification.class, id);
        if (notification != null) {
            notification.setRead(true);
            entityManager.merge(notification);
        }
        else {
            return ResponseEntity.badRequest().body(Map.of("message", "Notification not found"));
        }

        return ResponseEntity.ok().body(Map.of("message", "Notification marked as read"));
    }
}