package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductSupermarket;
import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.User;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user/request")
public class RequestController {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    @GetMapping
    public String request(@RequestParam(required = false, defaultValue = "false") boolean success, Model model,
            HttpSession session) {
        User requester = (User) session.getAttribute("u");
        List<Request> requests = entityManager
                .createQuery("SELECT r FROM Request r WHERE r.user.id = :uid ORDER BY r.date DESC", Request.class)
                .setParameter("uid", requester.getId())
                .getResultList();
        model.addAttribute("requests", requests);
        model.addAttribute("success", success);
        return "request";
    }

    @PostMapping
    @Transactional
    public String submitRequest(
            @RequestParam String name,
            @RequestParam String brand,
            @RequestParam float price,
            @RequestParam String supermarket,
            @RequestParam String ean,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam int type,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        User requester = (User) session.getAttribute("u");

        if (type != 0 && type != 1) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tipo de solicitud no válido");
            return null;
        }

        boolean addProduct = (type == 0);
        if (addProduct) {
            Product existingProduct = entityManager.createNamedQuery("Product.searchByEAN", Product.class)
                    .setParameter("EAN", ean.trim())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            //Caso 1: Se solicita añadir un producto que ya existe en el supermercado solicitado
            if (existingProduct != null) {
                for (ProductSupermarket p : existingProduct.getVinculaciones()) {
                    if (p.getSupermarket().getName().equalsIgnoreCase(supermarket.trim())) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                                "El producto ya existe en ese supermercado");
                        return null;
                    }
                }
            }
        } else {
            Product productToModify = entityManager.createNamedQuery("Product.searchByEAN", Product.class)
                    .setParameter("EAN", ean.trim())
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            //Caso 3: Se solicita modificar un producto existente, pero el EAN no existe o no coincide con el producto a modificar
            if (productToModify == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "No se puede modificar un producto que no existe");
                return null;
            } else {
                boolean existe = false;
                for (ProductSupermarket p : productToModify.getVinculaciones()) {
                    if (p.getSupermarket().getName().equalsIgnoreCase(supermarket.trim())) {
                        existe = true;
                        break;
                    }
                }
                if (!existe) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "No existe este producto en el supermercado indicado, no se puede modificar");
                    return null;
                }
            }
        }

        Request request = new Request();
        request.setName(name.trim());
        request.setBrand(brand.trim());
        request.setPrice(price);
        request.setSupermarket(supermarket.trim());
        request.setEAN(ean.trim());
        request.setQuantity("1");
        request.setType(type);
        request.setApproved(false);
        request.setDate(LocalDateTime.now());
        request.setUser(requester);
        entityManager.persist(request);

        return "redirect:/user/request?success=true";
    }
}
