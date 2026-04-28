package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
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
    public String search(
            @RequestParam(required = false) String producto, 
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        int pageSize = 15;
        int offset = (page - 1) * pageSize;

        if (producto == null || producto.trim().isEmpty()) {
            // Obtener pageSize los productos a partir del offset
            List<Product> productosList = entityManager
                    .createQuery("SELECT p FROM Product p", Product.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .getResultList();

            // Total de productos para numero de paginas
            Long total = entityManager
                    .createNamedQuery("Product.totalNum", Long.class)
                    .getSingleResult();

            // Objeto de paginacion para la vista
            Page<Product> todosLosProductos = new PageImpl<>(productosList, PageRequest.of(page - 1, pageSize), total);
            
            model.addAttribute("productos", todosLosProductos);
            model.addAttribute("url", "/search");

            return "search";
        }   
        model.addAttribute("url", "/search/" + producto);
        
        return "redirect:/search/" + producto;
    }

    @GetMapping("/{product}")
    @Transactional
    public String searchProduct(
            @PathVariable(name = "product") String producto, 
            @RequestParam(defaultValue = "1") int page, 
            Model model) {

        int pageSize = 6;
        int offset = (page - 1) * pageSize;

        List<Product> productos = new ArrayList<>();
        
        // Busco por EAN
        productos = entityManager
                .createNamedQuery("Product.searchByEAN", Product.class)
                .setParameter("EAN", producto)
                .getResultList();

        long total;

        if (productos.size() > 0) {
            total = 1;
        } else {
            // Si no se encuentra por EAN, busco por nombre 
            productos = entityManager
                .createNamedQuery("Product.searchByName", Product.class)
                .setParameter("name", "%" + producto + "%")
                .setFirstResult(offset)
                .setMaxResults(pageSize)
                .getResultList();

            total = entityManager
                    .createNamedQuery("Product.totalNumName", Long.class)
                    .setParameter("name", "%" + producto + "%")
                    .getSingleResult();
        }

        if (total == 1) {
            return "redirect:/product/" + productos.get(0).getId();
        } 
        else if (total == 0) {
            model.addAttribute("error", "No se han encontrado resultados para: " + producto);
        }

        Page<Product> productosPage = new PageImpl<>(productos, PageRequest.of(page - 1, pageSize), total);
        model.addAttribute("productos", productosPage);
        model.addAttribute("url", "/search/" + producto);

        return "search";
    }
}