package es.ucm.fdi.iw.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Cart;
import es.ucm.fdi.iw.model.ProductCart;


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
    public String cart( 
        @RequestParam(required = false) Long cartId,
        @RequestParam(defaultValue = "false") boolean editCart,
        HttpSession session,
        Model model) {
        
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
        model.addAttribute("editCart", editCart);

        return "cart";
    }

    @GetMapping("/{cartId}")
    public String userCart(
        @PathVariable long cartId, 
        HttpSession session, 
        Model model) {
            
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

    @Transactional
    @PostMapping("/rename")
    public String renameCart(
        @RequestParam long cartId,
        @RequestParam String rename,
        HttpSession session,
        Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = entityManager.find(Cart.class, cartId);

        if(cart == null) {
            model.addAttribute("errorMessage", "Carrito no encontrado.");
            return "cart";
        }
        if(cart.getUser().getId() != user.getId()) {
            model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
            return "cart";
        }
        
        cart.setName(rename);

        return "redirect:/cart?cartId=" + cartId;
    }

    @Transactional
    @PostMapping("/delete")
    public String deleteCart(
        @RequestParam long cartId,
        HttpSession session,
        Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = entityManager.find(Cart.class, cartId);

        if(cart == null) {
            model.addAttribute("errorMessage", "Carrito no encontrado.");
            return "cart";
        }
        if(cart.getUser().getId() != user.getId()) {
            model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
            return "cart";
        }
        
        entityManager.remove(cart);

        return "redirect:/cart";
    }

    @Transactional
    @PostMapping("/create")
    public String createCart(
        HttpSession session,
        Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = new Cart();
        cart.setName("Nuevo Carrito");
        cart.setUser(user);
        cart.setDate(LocalDateTime.now());
        cart.setItems(new ArrayList<>());
        entityManager.persist(cart);

        return "redirect:/cart?cartId=" + cart.getId();
    }

    @Transactional
    @PostMapping("/update")
    public String updateCart(
        @RequestParam long cartId,
        @RequestParam long itemId,
        @RequestParam String action,
        HttpSession session,
        Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        ProductCart item = entityManager.find(ProductCart.class, itemId);
        
        if(item == null) {
            model.addAttribute("errorMessage", "Producto no encontrado.");
            return "cart";
        }
        if(item.getCart().getId() != cartId) {
            model.addAttribute("errorMessage", "El producto no pertenece a este carrito.");
            return "cart";
        }
        if(user.getId() != item.getCart().getUser().getId()) {
            model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
            return "cart";
        }

        if("suma".equals(action)) {
            item.setQuantity(item.getQuantity() + 1);
        } else if("resta".equals(action)) {
            item.setQuantity(item.getQuantity() - 1);
            if(item.getQuantity() <= 0) {
                entityManager.remove(item);
            }
        }

        return "redirect:/cart?cartId=" + cartId;
    }
}