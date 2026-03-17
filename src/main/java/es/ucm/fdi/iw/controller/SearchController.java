package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductSupermarket;
import es.ucm.fdi.iw.model.Supermarket;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/search")
public class SearchController {


    @Autowired
    private EntityManager entityManager;

    /**
     * Landing page for a product
     */

    @GetMapping("")
    public String search(Model model, HttpServletRequest request) {
        return "search";
    }

    @GetMapping("/{product}")
    @Transactional
    public String search(@PathVariable(name = "product") String producto, Model model) {

        List<Product> productos = entityManager
                .createNamedQuery("Product.searchByNameOrEAN", Product.class)
                .setParameter("param", "%" + producto + "%")
                .getResultList();

        if (productos.size() == 1) {
            return "redirect:/product?productoID=" + productos.get(0).getId();
        }
        else if (productos.isEmpty() && producto != null) {
            model.addAttribute("error", "No se han encontrado resultados para '" + producto + "'.");
        }
        else {
            model.addAttribute("productos", productos);
        }

        return "search";
    }
}