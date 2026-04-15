package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private EntityManager entityManager;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    /**
     * Landing page for a product
     */

    @GetMapping
    public String search(@RequestParam(required = false) String producto, Model model, HttpServletRequest request) {
        if (producto == null || producto.trim().isEmpty()) {
            List<Product> todosLosProductos = entityManager
                    .createQuery("SELECT p FROM Product p", Product.class)
                    .getResultList();
            model.addAttribute("productos", todosLosProductos);
            return "search";
        }
        
        return "redirect:/search/" + producto;
    }

    @GetMapping("/{product}")
    @Transactional
    public String searchProduct(@PathVariable(name = "product") String producto, Model model) {

        List<Product> productos = new ArrayList<>();
        productos = entityManager
                .createNamedQuery("Product.searchByEAN", Product.class)
                .setParameter("EAN", producto)
                .getResultList();

        if (productos.size() == 0){
            productos = entityManager
                .createNamedQuery("Product.searchByName", Product.class)
                .setParameter("name", "%" + producto + "%")
                .getResultList();
        }

        if (productos.size() == 1) {
            return "redirect:/product/" + productos.get(0).getId();
        } 
        else if (productos.size() == 0) {
            model.addAttribute("error", "No se han encontrado resultados para el producto: " + producto);
        }

        model.addAttribute("productos", productos);

        return "search";
    }
}