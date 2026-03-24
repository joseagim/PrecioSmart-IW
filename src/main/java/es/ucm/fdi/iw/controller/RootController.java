package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.User;
import jakarta.persistence.EntityManager;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Non-authenticated requests only.
 */
@Controller
public class RootController {

    //private static final Logger log = LogManager.getLogger(RootController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }
    
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        boolean error = request.getQueryString() != null && request.getQueryString().indexOf("error") != -1;
        model.addAttribute("loginError", error);
        return "login";
    }

    @GetMapping("/authors")
    public String authors(Model model, HttpServletRequest request) {
        return "authors";

    }

    @GetMapping("/register")
    public String register(Model model, HttpServletRequest request) {
        return "register";
    }

    @PostMapping("/register")
    @Transactional
    public String postRegister(
        @RequestParam String username,
        @RequestParam(required = false) String email,
        @RequestParam String password,
        @RequestParam String confirmPassword,
        Model model
    ) {
        // email is currently collected in the form but not persisted in User
        if (email != null) {
            email = email.trim();
        }

        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.isEmpty() || password == null || password.isBlank()) {
            model.addAttribute("registerError", "Usuario y contrasena son obligatorios");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("registerError", "Las contrasenas no coinciden");
            return "register";
        }

        Long existing = entityManager.createNamedQuery("User.hasUsername", Long.class)
            .setParameter("username", normalizedUsername)
            .getSingleResult();

        if (existing != null && existing > 0) {
            model.addAttribute("registerError", "Ese nombre de usuario ya existe");
            return "register";
        }

        User user = new User();
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setRoles(User.Role.USER.toString());
        entityManager.persist(user);

        model.addAttribute("registerOk", "Usuario creado, ya puedes iniciar sesion");
        return "login";
    }

    @GetMapping("/user/notifications")
    public String notifications(Model model, HttpServletRequest request) {
        return "notifications";
    }


    @GetMapping("/faq")
    public String faq(Model model, HttpServletRequest request) {
        return "faq";
    }
    

    @GetMapping("/user")
    public String user(HttpSession session) {
        User u = (User) session.getAttribute("u");
        if (u == null) {
            return "redirect:/login";
        }
        return "redirect:/user/" + u.getId();
    }

    @GetMapping("/admin")
    public String admin(Model model, HttpServletRequest request) {
        return "admin";
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
         User user = (User) session.getAttribute("u");
        if(user != null) {
            return "redirect:/user";
        }
        return "index";
    }
}
