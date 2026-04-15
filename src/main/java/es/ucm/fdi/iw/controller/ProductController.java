package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.controller.UserController.NoEsTuPerfilException;
import es.ucm.fdi.iw.model.Cart;
import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductSupermarket;
import es.ucm.fdi.iw.model.Supermarket;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Controller
@RequestMapping("/product")
public class ProductController {

    private static final Logger log = LogManager.getLogger(ProductController.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LocalData localData;

    @ModelAttribute
    public void populateModel(HttpSession session, Model model) {
        for (String name : new String[] { "u", "url", "ws", "topics" }) {
            model.addAttribute(name, session.getAttribute(name));
        }
    }

    @Transactional
    @GetMapping("/{productoID}")
    public String product(@PathVariable(name = "productoID") Long productoID, Model model, HttpSession session) {
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
            } else {
                ProductSupermarket nullPS = new ProductSupermarket();
                nullPS.setSupermarket(supermarket);
                result.add(nullPS);
            }

        }

        User user = (User) session.getAttribute("u");
        if(user != null) {
            User u = entityManager.find(User.class, user.getId());
            List<Cart> carts = u.getCarts();
            model.addAttribute("carts", carts);
        }
        
        model.addAttribute("product", product);
        model.addAttribute("result", result);

        return "product";
    }

    // dado un producto y un supermercado:
    // - si existe el producto en el supermercado lo devuelve
    // - si no existe el producto en el supermercado devuelve el más parecido

    /**
     * Finds the product of a supermarket given a reference one.
     * In case it doesn't exist, returns the best similar one.
     * 
     * @param product       reference product to find
     * @param supermarketID supermarket to find
     * @return product of the supermarket (or the most similar to the reference)
     */
    protected ProductSupermarket productBySupermarket(Product product, long supermarketID) {

        // busco el producto en el supermercado
        List<ProductSupermarket> matches = entityManager
                .createNamedQuery("ProductSupermarket.findProductSupermarket", ProductSupermarket.class)
                .setParameter("supermarketId", supermarketID)
                .setParameter("productId", product.getId())
                .getResultList();

        ProductSupermarket ps = matches.isEmpty() ? null : matches.getFirst();

        // si el producto existe en dicho supermercado lo devuelvo
        if (ps != null)
            return ps;

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

    private static InputStream defaultPic() {
        return new BufferedInputStream(Objects.requireNonNull(
            ProductController.class.getClassLoader().getResourceAsStream(
                "static/img/default-product-pic.jpg")));
    }

    /**
     * Downloads a profile pic for a product
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @GetMapping("{productoID}/pic")
    public StreamingResponseBody getPic(@PathVariable long productoID) throws IOException {
        File f = localData.getFile("product", "" + productoID + ".jpg");
        InputStream in = new BufferedInputStream(f.exists() ? new FileInputStream(f) : ProductController.defaultPic());
        return os -> FileCopyUtils.copy(in, os);
    }

    /**
     * Uploads a profile pic for a product
     * 
     * @param id
     * @return
     * @throws IOException
     */
    @PostMapping("{productoID}/pic")
    @ResponseBody
    public String setPic(@RequestParam("photo") MultipartFile photo, @PathVariable long productoID,
        HttpServletResponse response, HttpSession session, Model model) throws IOException {

        Product target = entityManager.find(Product.class, productoID);
        model.addAttribute("product", target);

        // check permissions
        User requester = (User) session.getAttribute("u");
        if (requester.getId() != target.getId() &&
            !requester.hasRole(Role.ADMIN)) {
        throw new NoEsTuPerfilException();
        }

        log.info("Updating photo for product {}", productoID);
        File f = localData.getFile("product", "" + productoID + ".jpg");
        if (photo.isEmpty()) {
        log.info("failed to upload photo: emtpy file?");
        } else {
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
            byte[] bytes = photo.getBytes();
            stream.write(bytes);
            log.info("Uploaded photo for {} into {}!", productoID, f.getAbsolutePath());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.warn("Error uploading " + productoID + " ", e);
        }
        }
        return "{\"status\":\"photo uploaded correctly\"}";
    }
}