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
@RequestMapping("/product")
public class ProductController {


    @Autowired
    private EntityManager entityManager;

    /**
     * Landing page for a product
     */

    @Transactional
    @GetMapping("")
    public String product(@RequestParam(name = "productoID", required = true) Long productoID, Model model) {

        // validar el id del producto
        if (productoID == null || productoID <= 0) {
            return "error";
        }

        Product product = entityManager.find(Product.class, productoID);
        if (product == null) {
            return "error";
        }

        List<Supermarket> supermarkets = entityManager
            .createQuery("SELECT s FROM Supermarket s ORDER BY s.id", Supermarket.class)
            .getResultList();

        List<ProductSupermarket> result = new ArrayList<>();
        for (Supermarket supermarket : supermarkets) {
            ProductSupermarket ps = productBySupermarket(product, supermarket.getId());
            if (ps != null) {
                result.add(ps);
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("result", result);

        return "product";
    }

    // dado un producto y un supermercado:
    //  - si existe el producto en el supermercado lo devuelve
    //  - si no existe el producto en el supermercado devuelve el más parecido

    /**
     * Finds the product of a supermarket given a reference one.
     * In case it doesn't exist, returns the best similar one.
     * 
     * @param product reference product to find
     * @param supermarketID supermarket to find
     * @return product of the supermarket (or the most similar to the reference)
    */
    private ProductSupermarket productBySupermarket(Product product, long supermarketID) {

        // busco el producto en el supermercado
        List<ProductSupermarket> matches = entityManager
                .createNamedQuery("ProductSupermarket.findProductSupermarket", ProductSupermarket.class)
                .setParameter("supermarketId", supermarketID)
            .setParameter("productId", product.getId())
            .getResultList();

        ProductSupermarket ps = matches.isEmpty() ? null : matches.getFirst();

        // si el producto existe en dicho supermercado lo devuelvo
        if (ps != null) return ps;

        // busco el producto más parecido en dicho supermercado
        StringBuilder name = new StringBuilder(product.getName());
        ProductSupermarket ret = null;

        // por el momento buscamos de letra en letra (en revisión)
        while (ret == null && name.length() > 3) {
            List<Product> products = entityManager
                    .createNamedQuery("Product.searchByName", Product.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
            
            for (Product p : products) {
            matches = entityManager
                        .createNamedQuery("ProductSupermarket.findProductSupermarket", ProductSupermarket.class)
                        .setParameter("supermarketId", supermarketID)
                .setParameter("productId", p.getId())
                .setMaxResults(1)
                .getResultList();

            ret = matches.isEmpty() ? null : matches.getFirst();

                // si el producto existe en dicho supermercado lo devuelvo
                if (ret != null) {
                    break;
                }
            }

            name.deleteCharAt(name.length() - 1);
        }

        return ret;
    }
}