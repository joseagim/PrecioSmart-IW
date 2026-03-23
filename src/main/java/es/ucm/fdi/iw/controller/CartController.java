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
@RequestMapping("/user/cart")
public class CartController {

    @Autowired
    private EntityManager entityManager;

    private boolean isOwner(Cart cart, User user) {
        return cart != null
            && user != null
            && cart.getUser() != null
            && cart.getUser().getId() == user.getId();
    }

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
            .createNamedQuery("Cart.searchByUserId", Cart.class)
            .setParameter("userId", user.getId())
            .getResultList();

        model.addAttribute("carts", carts != null ? carts : new ArrayList<>());

        if (cartId != null) {
            Cart selectedCart = entityManager.find(Cart.class, cartId);

            if(selectedCart == null) {
                model.addAttribute("errorMessage", "Carrito no encontrado.");
                return "cart";
            }
            if(!isOwner(selectedCart, user)) {
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
            .createNamedQuery("Cart.searchByUserId", Cart.class)
            .setParameter("userId", user.getId())
            .getResultList();
        model.addAttribute("carts", carts != null ? carts : new ArrayList<>());

        Cart selectedCart = entityManager.find(Cart.class, cartId);

        if(selectedCart == null) {
            model.addAttribute("errorMessage", "Carrito no encontrado.");
            return "cart";
        }
        if(!isOwner(selectedCart, user)) {
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
        if(!isOwner(cart, user)) {
            model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
            return "cart";
        }
        
        cart.setName(rename);

        return "redirect:/user/cart?cartId=" + cartId;
    }

    @Transactional
    @PostMapping("/gestionCarrito")
    public String gestionCarrito(
        @RequestParam(required = false) long cartId,
        @RequestParam String action,
        HttpSession session,
        Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        if("new".equals(action)) {
            Cart cart = new Cart();
            cart.setName("Nuevo Carrito");
            cart.setUser(entityManager.find(User.class, user.getId()));
            cart.setDate(LocalDateTime.now());
            cart.setItems(new ArrayList<>());
            entityManager.persist(cart);
            model.addAttribute("selectedCart", cart);
            entityManager.flush();
        }
        else if("delete".equals(action)) {
            Cart cart = entityManager.find(Cart.class, cartId);

            if(cart == null) {
                model.addAttribute("errorMessage", "Carrito no encontrado.");
                return "cart";
            }
            if(!isOwner(cart, user)) {
                model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
                return "cart";
            }

            List<ProductCart> items = cart.getItems();
            if(items != null) {
                for(ProductCart item : items) {
                    entityManager.remove(item);
                }
            }
            entityManager.remove(cart);
            entityManager.flush();
            model.addAttribute("selectedCart", null);
        }
        List<Cart> carts = entityManager
            .createNamedQuery("Cart.searchByUserId", Cart.class)
            .setParameter("userId", user.getId())
            .getResultList();
        model.addAttribute("carts", carts != null ? carts : new ArrayList<>());

        return "cart :: #seccionGestionCarritos";
    }

    @Transactional
    @PostMapping("/update")
    public String updateCart(
        @RequestParam long cartId,
        @RequestParam(required = false) Long itemId,
        @RequestParam String action,
        HttpSession session,
        Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = entityManager.find(Cart.class, cartId);

        if(!isOwner(cart, user)) {
            model.addAttribute("errorMessage", "Carrito no encontrado o no eres el dueño.");
            return "cart";
        }

        if("deleteAll".equals(action)) {
            for(ProductCart pc : cart.getItems()) {
                entityManager.remove(pc);
            }
            cart.getItems().clear();
            model.addAttribute("selectedCart", cart);
            return "cart :: #tablaProductos";
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

        switch (action) {
            case "suma":
                item.setQuantity(item.getQuantity() + 1);
                break;

            case "resta":
                item.setQuantity(item.getQuantity() - 1);
                if(item.getQuantity() <= 0) {
                    entityManager.remove(item);
                }
                break;
            case "delete":
                entityManager.remove(item); 
                break;
            default:
                break;
        }

        model.addAttribute("selectedCart", entityManager.find(Cart.class, cartId));
        
        return "cart :: #tablaProductos";
    }
}