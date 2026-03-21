package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.User;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user/request")
public class RequestController {

    @Autowired
    private EntityManager entityManager;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @Transactional
    @GetMapping
    public String request(@RequestParam(required = false, defaultValue = "false") boolean success, Model model,
            HttpSession session) {
        User requester = (User) session.getAttribute("u");
        List<Request> requests = entityManager
                .createQuery("SELECT r FROM Request r WHERE r.user.id = :uid ORDER BY r.date DESC", Request.class)
                .setParameter("uid", requester.getId())
                .getResultList();
        model.addAttribute("requests", requests);
        model.addAttribute("success", success);
        return "request";
    }


    @PostMapping
    @Transactional
    public String submitRequest(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam float price,
            @RequestParam String market,
            @RequestParam String ean,
            @RequestParam("photo") MultipartFile photo,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        User requester = (User) session.getAttribute("u");
        if (requester == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return "redirect:/login";
        }
        if (photo == null || photo.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "redirect:/user/request?success=false";
        }

        Request request = new Request();
        request.setName(name.trim());
        request.setBrand(brand.trim());
        request.setPrice(price);
        request.setSupermarket(market.trim());
        request.setEAN(ean.trim());
        request.setQuantity("1");
        request.setApproved(false);
        request.setDate(LocalDateTime.now());
        request.setUser(requester);
        entityManager.persist(request);

        return "redirect:/user/request?success=true";
    }

}
