package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;

import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Cart;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private EntityManager entityManager;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    /**
     * Landing page for cart
     */

    @GetMapping
    public String cart(@RequestParam(required = false) Long cartId, HttpSession session, Model model) {
        
        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        List<Cart> carts = entityManager
            .createNamedQuery("Cart.searchByUser", Cart.class)
            .setParameter("user", user)
            .getResultList();

        model.addAttribute("carts", carts != null ? carts : new ArrayList<>());

        if (cartId != null) {
            Cart selectedCart = entityManager.find(Cart.class, cartId);

            if(selectedCart == null) {
                model.addAttribute("errorMessage", "Carrito no encontrado.");
                return "cart";
            }
            if(selectedCart.getUser().getId() != user.getId()) {
                model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
                return "cart";
            }

            model.addAttribute("selectedCart", selectedCart);
        }

        return "cart";
    }

    @GetMapping("/{cartId}")
    public String userCart(@PathVariable long cartId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        List<Cart> carts = entityManager
            .createNamedQuery("Cart.searchByUser", Cart.class)
            .setParameter("user", user)
            .getResultList();
        model.addAttribute("carts", carts != null ? carts : new ArrayList<>());

        Cart selectedCart = entityManager.find(Cart.class, cartId);

        if(selectedCart == null) {
            model.addAttribute("errorMessage", "Carrito no encontrado.");
            return "cart";
        }
        if(selectedCart.getUser().getId() != user.getId()) {
            model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
            return "cart";
        }

        model.addAttribute("selectedCart", selectedCart);

        return "cart";
    }
}