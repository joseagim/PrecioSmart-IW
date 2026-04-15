package es.ucm.fdi.iw.controller;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductSupermarket;
import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.RequestStatus;
import es.ucm.fdi.iw.model.RequestType;
import es.ucm.fdi.iw.model.Supermarket;
import es.ucm.fdi.iw.model.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import org.springframework.http.ResponseEntity;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user/request")
public class RequestController {

    private static final Logger log = LogManager.getLogger(UserController.class);

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
    @GetMapping
    public String request(@RequestParam(required = false, defaultValue = "false") boolean success, Model model,
            HttpSession session) {
        User requester = (User) session.getAttribute("u");
        List<Request> requests = entityManager
                .createQuery("SELECT r FROM Request r WHERE r.user.id = :uid ORDER BY r.date DESC", Request.class)
                .setParameter("uid", requester.getId())
                .getResultList();

        model.addAttribute("success", success);
        model.addAttribute("requests", requests);
        return "request";
    }

    @GetMapping("view")
    public String viewRequest(@RequestParam long id, Model model, HttpSession session) {
        User requester = (User) session.getAttribute("u");
        Request request = entityManager.find(Request.class, id);
        if (request == null || request.getUser().getId() != requester.getId()) {
            return "redirect:/user/request?success=false";
        }
        model.addAttribute("request", request);
        return "view-request";
    }

    @Transactional
    @GetMapping("{id}/pic")
    public StreamingResponseBody getPic(@PathVariable long id) throws IOException {
        Request req = entityManager.find(Request.class, id);
        File f = null;
        if (req != null && req.getUser() != null && req.getEAN() != null) {
            f = localData.getFile("request_user" + req.getUser().getId(), req.getEAN() + ".jpg");
        }

        InputStream in = new BufferedInputStream(
                (f != null && f.exists()) ? new FileInputStream(f) : defaultPic());
        return os -> FileCopyUtils.copy(in, os);
    }

    private static InputStream defaultPic() {
        return new BufferedInputStream(Objects.requireNonNull(
                RequestController.class.getClassLoader().getResourceAsStream("static/img/default-product-pic.jpg")));
    }

    @PostMapping
    @Transactional
    @ResponseBody
    public ResponseEntity<Map<String, String>> submitRequest(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String quantity,
            @RequestParam float price,
            @RequestParam String supermarket,
            @RequestParam String ean,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam String type,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        User requester = (User) session.getAttribute("u");
        if (requester == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Sesion no valida"));
        }

        RequestType requestType = parseType(type);
        if (requestType == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", "Tipo de solicitud no valido"));
        }

        String normalizedName = name == null ? "" : name.trim();
        String normalizedBrand = brand == null ? "" : brand.trim();
        String normalizedQuantity = quantity == null ? "" : quantity.trim();
        String normalizedSupermarket = supermarket == null ? "" : supermarket.trim();
        String normalizedEan = ean == null ? "" : ean.trim();

        if (normalizedSupermarket.isEmpty() || normalizedEan.isEmpty() || price <= 0) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Faltan datos obligatorios"));
        }

        Request request = new Request();
        Product existingProduct;

        if (requestType == RequestType.ADD) {
            existingProduct = entityManager.createNamedQuery("Product.searchByEAN", Product.class)
                    .setParameter("EAN", normalizedEan)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            // Caso 1: Se solicita añadir un producto que ya existe
            if (existingProduct != null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "El producto ya existe"));
            }
            if (normalizedName.isEmpty() || normalizedBrand.isEmpty() || normalizedQuantity.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message",
                        "Para un producto nuevo debes indicar nombre, marca y cantidad"));
            }

        } else {
            existingProduct = entityManager.createNamedQuery("Product.searchByEAN", Product.class)
                    .setParameter("EAN", normalizedEan)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            /*
             * Caso 2: Se solicita añadir un producto ya existente en el supermercado
             * solicitado que no existe
             * Case 3 Se solicita modificar un producto existente, pero el EAN no existe o
             * no coincide con el producto a modificar
             */
            if (existingProduct == null) {
                return ResponseEntity.badRequest().body(Map.of("status", "error", "message",
                        "El producto con ese EAN no existe"));
            } else {
                Supermarket supermarketEntity = entityManager
                        .createQuery("SELECT s FROM Supermarket s WHERE LOWER(s.name) = LOWER(:name)",
                                Supermarket.class)
                        .setParameter("name", normalizedSupermarket)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);
                boolean existe = supermarketEntity != null && entityManager.createQuery(
                        "SELECT ps FROM ProductSupermarket ps WHERE ps.product.id = :productId AND ps.supermarket.id = :supermarketId",
                        ProductSupermarket.class)
                        .setParameter("productId", existingProduct.getId())
                        .setParameter("supermarketId", supermarketEntity.getId())
                        .getResultStream()
                        .findFirst()
                        .isPresent();


                if (existe && requestType == RequestType.ADD_IN_SUPER) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("status", "error", "message", "El producto ya existe en ese supermercado"));
                } else if (!existe && requestType == RequestType.MODIFY) {
                    return ResponseEntity.badRequest().body(Map.of("status", "error", "message",
                            "No existe este producto en el supermercado indicado, no se puede modificar"));
                }

                normalizedName = existingProduct.getName();
                normalizedBrand = existingProduct.getBrand();
                normalizedQuantity = existingProduct.getQuantity();
                request.setImageUrl(existingProduct.getImageUrl());

            }

        }

        request.setName(normalizedName);
        request.setBrand(normalizedBrand);
        request.setPrice(price);
        request.setSupermarket(normalizedSupermarket);
        request.setEAN(normalizedEan);
        request.setQuantity(normalizedQuantity);
        request.setType(requestType);
        request.setStatus(RequestStatus.PENDING);
        request.setDate(LocalDateTime.now());
        request.setUser(requester);

        if (photo != null && !photo.isEmpty()) {
            request.setImageUrl(savePhoto(photo, requester.getId(), request.getEAN(), response));
        }

        entityManager.persist(request);

        return ResponseEntity.ok(Map.of("status", "ok", "message", "Solicitud guardada correctamente"));
    }

    private String savePhoto(MultipartFile photo, long id, String EAN,
            HttpServletResponse response) throws IOException {

        log.info("Updating photo for user {}", id);
        File f = localData.getFile("request_user" + id, "" + EAN + ".jpg");
        if (photo.isEmpty()) {
            log.info("failed to upload photo: emtpy file?");
        } else {
            try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
                byte[] bytes = photo.getBytes();
                stream.write(bytes);
                log.info("Uploaded photo for {} into {}!", id, f.getAbsolutePath());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                log.warn("Error uploading " + id + " ", e);
            }
        }
        return f.getPath();

    }

    private RequestType parseType(String type) {
        if (type == null) {
            return null;
        }
        return switch (type.trim()) {
            case "0", "ADD" -> RequestType.ADD;
            case "1", "ADD_IN_SUPER" -> RequestType.ADD_IN_SUPER;
            case "2", "MODIFY" -> RequestType.MODIFY;
            default -> null;
        };
    }
}
