package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductSupermarket;
import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.RequestStatus;
import es.ucm.fdi.iw.model.Supermarket;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

/**
 * Site administration.
 *
 * Access to this end-point is authenticated - see SecurityConfig
 */
@Controller
@RequestMapping("admin")
public class AdminController {

  @Autowired
  private EntityManager entityManager;

  @ModelAttribute
  public void populateModel(HttpSession session, Model model) {
    for (String name : new String[] { "u", "url", "ws", "topics" }) {
      model.addAttribute(name, session.getAttribute(name));
    }
  }

  private static final Logger log = LogManager.getLogger(AdminController.class);

  @GetMapping({ "/mod/{requestType}", "/mod" })
  public String getRequest(@PathVariable(name = "requestType", required = false) String type, Model model) {

    model.addAttribute("users", entityManager.createQuery("select u from User u").getResultList());
    List<Request> requests = new ArrayList<>();

    if (type == null || type.equals("pending")) {

      type = "pending";
      requests = entityManager.createNamedQuery("findByStatus", Request.class)
          .setParameter("status", RequestStatus.PENDING)
          .getResultList();

    } else if (type.equals("accepted")) {

      requests = entityManager.createNamedQuery("findByStatus", Request.class)
          .setParameter("status", RequestStatus.APPROVED)
          .getResultList();

    } else if (type.equals("rejected")) {

      requests = entityManager.createNamedQuery("findByStatus", Request.class)
          .setParameter("status", RequestStatus.REJECTED)
          .getResultList();

    } else {
      // manejar error, type no es pending, ni accepted ni rejected
    }

    model.addAttribute("requests", requests);
    model.addAttribute("type", type);

    return "admin";
  }

  @PostMapping("/mod/accept")
  @Transactional
  public ResponseEntity<Map<String, String>> acceptRequest(Model model, @RequestParam Long id) {

    // si no hay id
    if (id == null) {
      return ResponseEntity.badRequest().body(Map.of("message", "El ID de la solicitud es requerido"));
    }

    Request request = entityManager.find(Request.class, id);
    // si no existe la request
    if (request == null) {
      return ResponseEntity.badRequest().body(Map.of("message", "No se encontró la solicitud con el ID proporcionado"));
    }
    // si la request no está pendiente
    if (request.getStatus() != RequestStatus.PENDING) {
      return ResponseEntity.badRequest().body(Map.of("message", "La solicitud ya ha sido procesada"));
    }

    Product product = entityManager.createNamedQuery("Product.searchByEAN", Product.class)
      .setParameter("EAN", request.getEAN())
        .getResultStream()
        .findFirst()
        .orElse(null);

    Supermarket supermarket = entityManager.createNamedQuery("Supermarket.searchByName", Supermarket.class)
        .setParameter("name", request.getSupermarket())
        .getSingleResult();

    // el supermercado no existe
    if (supermarket == null) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", "No se encontró el supermercado con el nombre proporcionado"));
    }

    // aceptamos la request según su tipo
    switch (request.getType()) {
      // añadir product y product supermarket
      case ADD: {
        // el producto ya existe
        if (product != null) {
          return ResponseEntity.badRequest().body(Map.of("message", "El producto ya existe"));
        }

        // el producto no existe, lo creamos
        product = new Product();
        product.setEAN(request.getEAN());
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setQuantity(request.getQuantity());

        // añadimos el precio en el super
        ProductSupermarket ps = new ProductSupermarket();
        ps.setProduct(product);
        ps.setSupermarket(supermarket);
        ps.setPrice(request.getPrice());
        ps.setDate(request.getDate());

        // ponemos la request como aceptada
        request.setStatus(RequestStatus.APPROVED);

        // guardamos en la bbdd
        entityManager.merge(request);
        entityManager.persist(product);
        entityManager.persist(ps);

        copyImageToProduct(request, product.getId());
        break;
      }
      // añadir product supermarket
      case ADD_IN_SUPER: {

          // el producto no existe
          if (product == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "El producto no existe"));
          }

        break;
      }
      // añadir product supermarket
      case MODIFY: {

            // el producto no existe
            if (product == null) {
              return ResponseEntity.badRequest().body(Map.of("message", "El producto no existe"));
            }

        break;
      }
      default: {

      }
    }

    return ResponseEntity.ok().body(Map.of("message", "Solicitud aceptada correctamente"));
  }

  @PostMapping("/mod/reject")
  @Transactional
  public ResponseEntity<Map<String, String>> rejectRequest(Model model, @RequestParam Long id) {
    if (id == null) {
      return ResponseEntity.badRequest().body(Map.of("message", "El ID de la solicitud es requerido"));
    }

    Request request = entityManager.find(Request.class, id);
    if (request == null) {
      return ResponseEntity.badRequest().body(Map.of("message", "No se encontró la solicitud con el ID proporcionado"));
    }
    if (request.getStatus() != RequestStatus.PENDING) {
      return ResponseEntity.badRequest().body(Map.of("message", "La solicitud ya ha sido procesada"));
    }

    request.setStatus(RequestStatus.REJECTED);
    entityManager.merge(request);

    return ResponseEntity.ok().body(Map.of("message", "Solicitud rechazada correctamente"));
  }

  void copyImageToProduct(Request request, long productId) {
    // 1. Definimos la ruta base (donde vive 'iwdata')
    // Es buena práctica tener esto en una constante o configuración
    String baseDir = "iwdata";

    // 2. Construimos los Paths de origen y destino
    // Origen: iwdata/request/{id}.jpg
    Path source = Paths.get(baseDir, "request", request.getId() + ".jpg");

    // Destino: iwdata/product/{id}.jpg
    Path target = Paths.get(baseDir, "product", productId + ".jpg");

    try {
      // 3. Verificamos si la imagen de la request existe antes de copiarla
      if (Files.exists(source)) {

        // 4. Aseguramos que la carpeta de destino existe (por si acaso)
        Files.createDirectories(target.getParent());

        // 5. Copiamos el archivo
        // REPLACE_EXISTING sirve para que, si ya había una foto vieja del producto, la
        // sobreescriba
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
      } else
        throw new IOException("La imagen no existe");
    } catch (IOException e) {
      // Las operaciones de archivos siempre pueden fallar (permisos, disco lleno,
      // etc.)
      System.err.println("Error al copiar la imagen: " + e.getMessage());
      e.printStackTrace();
    }
  }
}