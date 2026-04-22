package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ProductRepository productRepository;


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

        Pageable pageable = PageRequest.of(page, 15);

        if (producto == null || producto.trim().isEmpty()) {
            // Obtenemos una página de productos en lugar de la lista completa
            Page<Product> todosLosProductos = productRepository.findAll(pageable);
            
            model.addAttribute("productos", todosLosProductos);
            return "search";
        }
        
        // Al redirigir, pasamos el término de búsqueda para que el método searchProduct lo procese
        return "redirect:/search/" + producto;
    }

    @GetMapping("/{product}")
    @Transactional
    public String searchProduct(
            @PathVariable(name = "product") String producto, 
            @RequestParam(defaultValue = "1") int page, 
            Model model) {

        Pageable pageable = PageRequest.of(page, 6);
        
        // Buscamos primero por EAN (suponiendo que EAN es único, devolverá 1 o 0 resultados)
        Page<Product> productos = productRepository.findByEAN(producto, pageable);

        if (productos.isEmpty()) {
            productos = productRepository.findByNameContainingIgnoreCase(producto, pageable);
        }

        if (productos.getTotalElements() == 1) {
            return "redirect:/product/" + productos.getContent().get(0).getId();
        } 
        else if (productos.getTotalElements() == 0) {
            model.addAttribute("error", "No se han encontrado resultados para el producto: " + producto);
        }

        model.addAttribute("productos", productos);

        return "search";
    }
}