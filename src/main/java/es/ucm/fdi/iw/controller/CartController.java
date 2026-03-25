package es.ucm.fdi.iw.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.Cart;
import es.ucm.fdi.iw.model.Product;
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

            if (selectedCart == null) {
                model.addAttribute("errorMessage", "Carrito no encontrado.");
                return "cart";
            }
            if (!isOwner(selectedCart, user)) {
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

        if (selectedCart == null) {
            model.addAttribute("errorMessage", "Carrito no encontrado.");
            return "cart";
        }
        if (!isOwner(selectedCart, user)) {
            model.addAttribute("errorMessage", "No eres el dueño de este carrito.");
            return "cart";
        }

        model.addAttribute("selectedCart", selectedCart);

        return "cart";
    }

    @Transactional
    @PostMapping("/gestionCarrito")
    public String gestionCarrito(
            @RequestParam(required = false) Long cartId,
            @RequestParam String action,
            @RequestParam(required = false) String renameCart,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return "redirect:/login";
        }

        if ("newCart".equals(action)) {
            Cart cart = new Cart();
            cart.setName("Nuevo Carrito");
            cart.setUser(entityManager.find(User.class, user.getId()));
            cart.setDate(LocalDateTime.now());
            cart.setItems(new ArrayList<>());
            entityManager.persist(cart);
            model.addAttribute("selectedCart", cart);

            List<Cart> carts = entityManager
                    .createNamedQuery("Cart.searchByUserId", Cart.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
            model.addAttribute("carts", carts);

            return "redirect:/user/cart?cartId=" + cart.getId();
        }

        if (cartId == null)
            return "redirect:/user/cart";

        Cart cart = entityManager.find(Cart.class, cartId);
        if (cart == null || !isOwner(cart, user)) {
            model.addAttribute("errorMessage", "Carrito no encontrado o no eres el dueño de este carrito.");
            return "redirect:/user/cart";
        }

        if ("renameCart".equals(action)) {
            if (renameCart != null && !renameCart.isBlank()) {
                cart.setName(renameCart);
            }
            return "redirect:/user/cart?cartId=" + cartId;
        } else {

            List<ProductCart> items = cart.getItems();
            if (items != null) {
                for (ProductCart item : items) {
                    entityManager.remove(item);
                }
            }
            entityManager.remove(cart);
            return "redirect:/user/cart";
        }
    }

    @Transactional
    @PostMapping("/add/{productId}")
    @ResponseBody // Indica que devolvemos JSON y no una vista HTML
    public Map<String, Object> addToCart(
            @PathVariable long productId,
            @RequestParam(name = "cantidad", defaultValue = "1") int cantidad,
            @RequestParam(name = "cartId", required = false) Long cartId,
            HttpSession session) {

        User user = (User) session.getAttribute("u");
        if (user == null) {
            return Map.of("error", "Usuario no autenticado");
        }

        Product product = entityManager.find(Product.class, productId);
        if (product == null) {
            return Map.of("error", "Producto no encontrado");
        }

        int quantity = cantidad > 0 ? cantidad : 1;

        Cart cart = null;
        if (cartId != null) {
            cart = entityManager.find(Cart.class, cartId);
            if (!isOwner(cart, user)) {
                return Map.of("error", "Carrito no encontrado o no permitido");
            }
        }

        if (cart == null) {
            cart = new Cart();
            cart.setName("Nuevo Carrito");
            cart.setUser(entityManager.find(User.class, user.getId()));
            cart.setDate(LocalDateTime.now());
            cart.setItems(new ArrayList<>());
            entityManager.persist(cart);
        }

        ProductCart existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            ProductCart newItem = new ProductCart();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            entityManager.persist(newItem);
            cart.getItems().add(newItem);
        }
        
        return Map.of("success", "Producto añadido al carrito");
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

        if (!isOwner(cart, user)) {
            model.addAttribute("errorMessage", "Carrito no encontrado o no eres el dueño.");
            return "cart";
        }

        if ("deleteAll".equals(action)) {
            for (ProductCart pc : cart.getItems()) {
                entityManager.remove(pc);
            }
            cart.getItems().clear();
            model.addAttribute("selectedCart", cart);
            return "cart :: #tablaProductos";
        }

        ProductCart item = entityManager.find(ProductCart.class, itemId);

        if (item == null) {
            model.addAttribute("errorMessage", "Producto no encontrado.");
            return "cart";
        }
        if (item.getCart().getId() != cartId) {
            model.addAttribute("errorMessage", "El producto no pertenece a este carrito.");
            return "cart";
        }

        switch (action) {
            case "sumaProd":
                item.setQuantity(item.getQuantity() + 1);
                break;

            case "restaProd":
                item.setQuantity(item.getQuantity() - 1);
                if (item.getQuantity() <= 0) {
                    entityManager.remove(item);
                }
                break;
            case "deleteProd":
                entityManager.remove(item);
                break;
            default:
                break;
        }

        model.addAttribute("selectedCart", entityManager.find(Cart.class, cartId));

        return "cart :: #tablaProductos";
    }

}