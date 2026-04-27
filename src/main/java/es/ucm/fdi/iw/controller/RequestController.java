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
        List<Request> requests = entityManager.createNamedQuery("Request.findByUser", Request.class)
                .setParameter("uid", requester.getId()).getResultList();

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
        if (req != null) {
            if (req.getType() == RequestType.ADD) {
                f = localData.getFile("request", req.getId() + ".jpg");
            } else {
                // Si no hay imagen específica para la solicitud, intentamos mostrar la imagen
                // del producto si existe.
                Product product = entityManager.createNamedQuery("Product.searchByEAN", Product.class)
                        .setParameter("EAN", req.getEAN())
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (product != null) {
                    f = localData.getFile("product", product.getId() + ".jpg");
                }
            }
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
            return ResponseEntity.badRequest().body(Map.of("message", "Sesion no valida"));
        }

        RequestType requestType = parseType(type);
        if (requestType == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Tipo de solicitud no valido"));
        }

        String normalizedName = name == null ? "" : name.trim();
        String normalizedBrand = brand == null ? "" : brand.trim();
        String normalizedQuantity = quantity == null ? "" : quantity.trim();
        String normalizedSupermarket = supermarket == null ? "" : supermarket.trim();
        String normalizedEan = ean == null ? "" : ean.trim();

        if (normalizedSupermarket.isEmpty() || normalizedEan.isEmpty() || price <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Faltan datos obligatorios"));
        }

        Request request = new Request();
        String msg = null;

        Product p = entityManager.createNamedQuery(
                "Product.searchByEAN", Product.class)
                .setParameter("EAN", normalizedEan)
                .getResultStream()
                .findFirst()
                .orElse(null);
        Supermarket s = entityManager.createNamedQuery(
                "Supermarket.searchByName", Supermarket.class)
                .setParameter("name", normalizedSupermarket)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (requestType == RequestType.ADD) {
            if (p != null && s != null) {
                ProductSupermarket ps = entityManager.createNamedQuery(
                        "ProductSupermarket.findProductSupermarket", ProductSupermarket.class)
                        .setParameter("supermarketId", s.getId())
                        .setParameter("productId", p.getId())
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                normalizedName = p.getName();
                if (ps != null) {
                    requestType = RequestType.MODIFY;
                    msg = "El producto " + p.getName() + ", ya existe en el supermercado "
                            + s.getName()
                            + ", se ha cambiado el tipo de solicitud a MODIFICAR para cambiar su precio.";

                } else {
                    requestType = RequestType.ADD_IN_SUPER;
                    msg = "El producto " + p.getName()
                            + " existe en la base de datos pero no existe en el supermercado "
                            + s.getName()
                            + ", se ha cambiado el tipo de solicitud a AÑADIR EN SUPERMERCADO para añadirlo.";

                }
                normalizedName = p.getName();
                normalizedBrand = p.getBrand();
                normalizedQuantity = p.getQuantity();

            }
        } else {
            if (p == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "No existe ningún producto con ese EAN, cambia el tipo de"
                                + "solicitud a AÑADIR para añadir el producto a la base de datos."));
            } else {
                ProductSupermarket ps = entityManager.createNamedQuery(
                        "ProductSupermarket.findProductSupermarket", ProductSupermarket.class)
                        .setParameter("supermarketId", s.getId())
                        .setParameter("productId", p.getId())
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                if (ps != null && requestType == RequestType.ADD_IN_SUPER) {
                    requestType = RequestType.MODIFY;
                    msg = "El producto " + p.getName() + ", ya existe en el supermercado "
                            + s.getName() + ", se ha cambiado el tipo de solicitud a MODIFICAR para cambiar su precio.";
                } else if (ps == null && requestType == RequestType.MODIFY) {
                    requestType = RequestType.ADD_IN_SUPER;
                    msg = "El producto " + p.getName() + " no existe en el supermercado "
                            + s.getName()
                            + ", se ha cambiado el tipo de solicitud a AÑADIR EN SUPERMERCADO para añadirlo.";
                }
                normalizedName = p.getName();
                normalizedBrand = p.getBrand();
                normalizedQuantity = p.getQuantity();
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

        // Persistimos primero para obtener el ID autogenerado y usarlo en el nombre de
        // la imagen.
        entityManager.persist(request);
        entityManager.flush();

        if (photo != null && !photo.isEmpty()) {
            request.setImageUrl(savePhoto(photo, requester.getId(), request.getId(), response));
        }

        return ResponseEntity.ok(
                Map.of("message", (msg != null) ? msg : "Solicitud guardada correctamente"));
    }

    private String savePhoto(MultipartFile photo, long id, long IDRequest,
            HttpServletResponse response) throws IOException {

        log.info("Updating photo for user {}", id);
        File f = localData.getFile("request", "" + IDRequest + ".jpg");
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
