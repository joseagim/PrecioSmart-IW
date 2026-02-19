package es.ucm.fdi.iw.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import es.ucm.fdi.iw.Carrito;
import es.ucm.fdi.iw.Product;


/**
 *  Non-authenticated requests only.
 */
@Controller
public class RootController {

    private static final Logger log = LogManager.getLogger(RootController.class);

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {        
        for (String name : new String[] { "u", "url", "ws", "topics"}) {
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
    public String posRegister(@RequestBody String entity) {
        //TODO: REGISTRAR USUARIO
        //Crear clase DTO(UserRegisterDTO) o con @RequestParam para recoger los datos del formulario de registro
        //Lógica para registrar al usuario en la base de datos
        return "login";
    }
    
    @GetMapping("/cart")
    public String cart(Model model, HttpServletRequest request) {
        return "cart";
    }

    @GetMapping("/search")
    public String search(Model model, HttpServletRequest request) {
        return "search";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, HttpServletRequest request) {
        return "notifications";
    }    
    
    @GetMapping("/faq")
    public String faq(Model model, HttpServletRequest request) {
        return "faq";
    }

    @GetMapping("/user")
    public String user(Model model, HttpServletRequest request) {
        return "user";
    }

	@GetMapping("/")
    public String index(Model model) {
        return "index";
    }
}
