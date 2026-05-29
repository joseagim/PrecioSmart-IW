package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.controller.UserController.NoEsTuPerfilException;
import es.ucm.fdi.iw.model.Notification;
import es.ucm.fdi.iw.model.Product;
import es.ucm.fdi.iw.model.ProductSupermarket;
import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.RequestStatus;
import es.ucm.fdi.iw.model.RequestType;
import es.ucm.fdi.iw.model.Supermarket;
import es.ucm.fdi.iw.model.User;
import es.ucm.fdi.iw.model.User.Role;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;
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

  @Autowired
  private SimpMessagingTemplate messagingTemplate;

  @Autowired
  private LocalData localData;

  @ModelAttribute
  public void populateModel(HttpSession session, Model model) {
    for (String name : new String[] { "u", "url", "ws", "topics" }) {
      model.addAttribute(name, session.getAttribute(name));
    }
  }

  private static final Logger log = LogManager.getLogger(AdminController.class);

  @GetMapping
  public String admin(HttpSession session, Model model) {

    User user = (User) session.getAttribute("u");
    if (user == null) {
      return "redirect:/login";
    }
    if (!user.hasRole(Role.ADMIN)) {
      return "index";
    }
    return "admin";
  }

  @GetMapping({ "/mod/{requestType}", "/mod" })
  public String getRequest(@PathVariable(name = "requestType", required = false) String type, Model model) {

    model.addAttribute("users", entityManager.createQuery("select u from User u").getResultList());
    List<Request> requests = new ArrayList<>();

    if (type == null || type.equals("pending")) {

      type = "pending";
      requests = entityManager.createNamedQuery("Request.findByStatus", Request.class)
          .setParameter("status", RequestStatus.PENDING)
          .getResultList();

    } else if (type.equals("accepted")) {

      requests = entityManager.createNamedQuery("Request.findByStatus", Request.class)
          .setParameter("status", RequestStatus.APPROVED)
          .getResultList();

    } else if (type.equals("rejected")) {

      requests = entityManager.createNamedQuery("Request.findByStatus", Request.class)
          .setParameter("status", RequestStatus.REJECTED)
          .getResultList();

    } else {
      // manejar error, type no es pending, ni accepted ni rejected
    }

    model.addAttribute("requests", requests);
    model.addAttribute("type", type);
    model.addAttribute("admin", "requests");

    return "admin";
  }

  @PostMapping("/mod/accept")
  @Transactional
  public ResponseEntity<Map<String, String>> acceptRequest(Model model, @RequestParam Long id) {

    // si no hay id
    if (id == null) {
      return ResponseEntity.badRequest().body(Map.of("message",
          "El ID de la solicitud es requerido"));
    }

    Request request = entityManager.find(Request.class, id);

    // si no existe la request
    if (request == null) {
      return ResponseEntity.badRequest().body(Map.of("message",
          "No se encontró la solicitud con el ID proporcionado"));
    }
    // si la request no está pendiente
    if (request.getStatus() != RequestStatus.PENDING) {
      return ResponseEntity.badRequest().body(Map.of("message",
          "La solicitud ya ha sido procesada"));
    }

    Product product = entityManager.createNamedQuery("Product.searchByEAN",
        Product.class)
        .setParameter("EAN", request.getEAN())
        .getResultStream()
        .findFirst()
        .orElse(null);

    Supermarket supermarket = entityManager.createNamedQuery("Supermarket.searchByName",
        Supermarket.class)
        .setParameter("name", request.getSupermarket())
        .getSingleResult();

    // el supermercado no existe
    if (supermarket == null) {
      return ResponseEntity.badRequest()
          .body(Map.of("message", "No se encontró el supermercado con el nombre proporcionado"));
    }

    // AÑADIR NUEVO PRODUCTO
    if (request.getType() == RequestType.ADD) {
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

      entityManager.persist(product);
      copyImageToProduct(request, product.getId());
    }
    // AÑADIR/MODIFICAR PRODUCTO EN SUPER
    else if (request.getType() == RequestType.ADD_IN_SUPER ||
        request.getType() == RequestType.MODIFY) {
      // el producto no existe
      if (product == null) {
        return ResponseEntity.badRequest().body(Map.of("message", "El producto no existe"));
      }
      // miro si existe el product supermarket
      List<ProductSupermarket> matches = entityManager
          .createNamedQuery("ProductSupermarket.findProductSupermarket", ProductSupermarket.class)
          .setParameter("supermarketId", supermarket.getId())
          .setParameter("productId", product.getId())
          .setMaxResults(1)
          .getResultList();

      ProductSupermarket ps = matches.isEmpty() ? null : matches.getFirst();

      // si ya existe
      if (ps != null && request.getType() == RequestType.ADD_IN_SUPER) {
        return ResponseEntity.badRequest().body(Map.of("message",
            "El producto ya existe ese supermercado"));
      } else if (ps == null && request.getType() == RequestType.MODIFY) {
        return ResponseEntity.badRequest().body(Map.of("message",
            "El producto no existe en este supermecado"));
      }
    } else {
      return ResponseEntity.badRequest()
          .body(Map.of("message", "El tipo de la solicitud no es válido"));
    }

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

    try {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(
          Map.of("tipo", "request", "resultado", "aceptada"));
      messagingTemplate.convertAndSend("/user/"
          + request.getUser().getUsername() + "/queue/updates", json);
    } catch (Exception e) {
      log.warn("error serializando json", e);
    }
    // crear noti para mandársela al usuario
    Notification notification = new Notification();
    notification.setUser(request.getUser());
    notification.setRequest(request);
    notification.setRead(false);
    notification.setDate(LocalDateTime.now());
    entityManager.persist(notification);
    return ResponseEntity.ok().body(Map.of("message", "Solicitud aceptada correctamente"));

  }

  @PostMapping("/mod/reject")
  @Transactional
  public ResponseEntity<Map<String, String>> rejectRequest(Model model, @RequestParam Long id) {
    if (id == null) {
      return ResponseEntity.badRequest().body(Map.of("message",
          "El ID de la solicitud es requerido"));
    }

    Request request = entityManager.find(Request.class, id);
    if (request == null) {
      return ResponseEntity.badRequest().body(Map.of("message",
          "No se encontró la solicitud con el ID proporcionado"));
    }
    if (request.getStatus() != RequestStatus.PENDING) {
      return ResponseEntity.badRequest().body(Map.of("message",
          "La solicitud ya ha sido procesada"));
    }

    request.setStatus(RequestStatus.REJECTED);
    entityManager.merge(request);

    try {
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(
          Map.of("tipo", "request", "resultado", "rechazada"));
      messagingTemplate.convertAndSend("/user/"
          + request.getUser().getUsername() + "/queue/updates", json);
    } catch (Exception e) {
      log.warn("error serializando json", e);
    }

    // crear noti para mandársela al usuario
    Notification notification = new Notification();
    notification.setUser(request.getUser());
    notification.setRequest(request);
    notification.setRead(false);
    notification.setDate(LocalDateTime.now());
    entityManager.persist(notification);
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

  // SUPERMERCADOS:

  @GetMapping({ "/supermarkets" })
  public String getSupermarkets(@RequestParam(defaultValue = "1") int page, Model model) {

    int pageSize = 3;
    int offset = (page - 1) * pageSize;

    List<Supermarket> supermarketList = entityManager
        .createQuery("SELECT s FROM Supermarket s", Supermarket.class)
        .setFirstResult(offset)
        .setMaxResults(pageSize)
        .getResultList();
    Long total = entityManager
        .createNamedQuery("Supermarket.totalNum", Long.class)
        .getSingleResult();

    Page<Supermarket> todosLosSupermercados = new PageImpl<>(supermarketList, PageRequest.of(page - 1, pageSize),
        total);

    model.addAttribute("supermarkets", todosLosSupermercados);
    model.addAttribute("admin", "supermarkets");
    model.addAttribute("paginationUrl", "/admin/supermarkets");

    return "admin";
  }

  @GetMapping({ "/supermarkets/{supermarketID}" })
  public String supermarket(@PathVariable(name = "supermarketID") Long supermarketID, Model model) {

    if (supermarketID == null) {
      return "error";
    } else if (supermarketID == 0) {
      return "supermarket";
    }

    Supermarket supermarket = entityManager.find(Supermarket.class, supermarketID);
    if (supermarket == null) {
      return "error";
    }

    model.addAttribute("supermarket", supermarket);

    return "supermarket";
  }

  @Transactional
  @PostMapping("/supermarkets/edit")
  public String editarSupermarket(
      @RequestParam Long supermarketID,
      @RequestParam String name,
      @RequestParam String info,
      @RequestParam(required = false, defaultValue = "") MultipartFile photo,
      HttpServletResponse response,
      HttpSession session,
      Model model) {

    if (supermarketID == null || supermarketID <= 0) {
      return "error";
    }

    Supermarket supermarket = entityManager.find(Supermarket.class, supermarketID);
    if (supermarket == null) {
      return "error";
    }

    supermarket.setName(name.trim());
    supermarket.setInfo(info.trim());
    entityManager.persist(supermarket);

    if (photo != null && !photo.isEmpty()) {
      try {
        setSupermarketPic(photo, supermarketID, response, session, model);
      } catch (Exception e) {
        log.warn("Error", e);
      }
    }
    return "redirect:/admin/supermarkets";
  }

  @Transactional
  @PostMapping("/supermarkets/create")
  public String crearSupermarket(
      @RequestParam String name,
      @RequestParam String info,
      @RequestParam(required = false, defaultValue = "") MultipartFile photo,
      HttpServletResponse response,
      HttpSession session,
      Model model) {

    User requester = (User) session.getAttribute("u");
    if (!requester.hasRole(Role.ADMIN)) {
      throw new NoEsTuPerfilException();
    }

    Supermarket supermarket = new Supermarket();

    supermarket.setName(name.trim());
    supermarket.setInfo(info.trim());
    entityManager.persist(supermarket);

    if (photo != null && !photo.isEmpty()) {
      try {
        setSupermarketPic(photo, supermarket.getId(), response, session, model);
      } catch (Exception e) {
        log.warn("Error", e);
      }
    }

    return "redirect:/admin/supermarkets";
  }

  @Transactional
  @PostMapping("/supermarkets/delete")
  public String eliminarSupermarket(
      @RequestParam Long supermarketID,
      HttpSession session,
      Model model) {

    // 1. Control de acceso y seguridad: Verificar que sea ADMIN
    User requester = (User) session.getAttribute("u");
    if (requester == null || !requester.hasRole(Role.ADMIN)) {
      throw new NoEsTuPerfilException();
    }

    if (supermarketID == null || supermarketID <= 0) {
      return "error";
    }

    // 2. Evitar fallos de integridad estructural (Clave Foránea)
    // Eliminamos primero todas las vinculaciones de precios de este supermercado
    entityManager.createQuery("DELETE FROM ProductSupermarket ps WHERE ps.supermarket.id = :id")
        .setParameter("id", supermarketID)
        .executeUpdate();

    // 3. Buscar y eliminar el supermercado de la base de datos
    Supermarket supermarket = entityManager.find(Supermarket.class, supermarketID);
    if (supermarket == null) {
      return "error";
    }

    entityManager.remove(supermarket);

    // Opcional: Eliminar el archivo de imagen del disco si existe para no dejar basura
    File foto = localData.getFile("supermarket", "" + supermarketID + ".jpg");
    if (foto.exists()) {
        foto.delete();
    }

    // Redirigir de nuevo a la lista actualizada de supermercados
    return "redirect:/admin/supermarkets";
  }

  private static InputStream SupermarketDefaultPic() {
    return new BufferedInputStream(Objects.requireNonNull(
        AdminController.class.getClassLoader().getResourceAsStream(
            "static/img/default-supermarket-pic.jpg")));
  }

  @GetMapping("/supermarkets/{supermercadoID}/pic")
  public StreamingResponseBody getSupermarketPic(@PathVariable long supermercadoID) throws IOException {
    File f = localData.getFile("supermarket", "" + supermercadoID + ".jpg");
    InputStream in = new BufferedInputStream(
        f.exists() ? new FileInputStream(f) : AdminController.SupermarketDefaultPic());
    return os -> FileCopyUtils.copy(in, os);
  }

  @GetMapping("/supermarkets/picByName/{name}")
  public StreamingResponseBody getSupermarketPicByName(@PathVariable String name) throws IOException {
    List<Supermarket> found = entityManager.createNamedQuery("Supermarket.searchByName", Supermarket.class)
        .setParameter("name", name)
        .setMaxResults(1)
        .getResultList();
    long id = found.get(0).getId();
    File f = localData.getFile("supermarket", "" + id + ".jpg");
    InputStream in = new BufferedInputStream(
        f.exists() ? new FileInputStream(f) : AdminController.SupermarketDefaultPic());
    return os -> FileCopyUtils.copy(in, os);
  }

  public String setSupermarketPic(MultipartFile photo, long supermercadoID,
      HttpServletResponse response, HttpSession session, Model model) throws IOException {

    Supermarket target = entityManager.find(Supermarket.class, supermercadoID);
    model.addAttribute("supermarket", target);

    // check permissions
    User requester = (User) session.getAttribute("u");
    if (requester.getId() != target.getId() &&
        !requester.hasRole(Role.ADMIN)) {
      throw new NoEsTuPerfilException();
    }

    log.info("Updating photo for supermarket {}", supermercadoID);
    File f = localData.getFile("supermarket", "" + supermercadoID + ".jpg");
    if (photo.isEmpty()) {
      log.info("failed to upload photo: emtpy file?");
    } else {
      try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
        byte[] bytes = photo.getBytes();
        stream.write(bytes);
        log.info("Uploaded photo for {} into {}!", supermercadoID, f.getAbsolutePath());
      } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log.warn("Error uploading " + supermercadoID + " ", e);
      }
    }
    return "{\"status\":\"photo uploaded correctly\"}";
  }

  @GetMapping("/users")
  public String listarUsuarios(
          @RequestParam(required = false, defaultValue = "all") String filter,
          HttpSession session, 
          Model model) {
      
      User requester = (User) session.getAttribute("u");
      if (requester == null || !requester.hasRole(Role.ADMIN)) {
          log.warn("Intento de acceso no autorizado a la gestión de usuarios por parte de un cliente.");
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Se requieren permisos de Administrador.");
      }

      List<User> listaUsuarios;

      switch (filter) {
          case "active":
              listaUsuarios = entityManager
                  .createQuery("SELECT u FROM User u WHERE u.enabled = true ORDER BY u.username ASC", User.class)
                  .getResultList();
              break;
              
          case "banned":
              listaUsuarios = entityManager
                  .createQuery("SELECT u FROM User u WHERE u.enabled = false ORDER BY u.username ASC", User.class)
                  .getResultList();
              break;
              
          case "all":
          default:
              listaUsuarios = entityManager
                  .createQuery("SELECT u FROM User u ORDER BY u.username ASC", User.class)
                  .getResultList();
              filter = "all";
              break;
      }

      model.addAttribute("users", listaUsuarios);
      model.addAttribute("currentFilter", filter); 
      model.addAttribute("admin", "users");

      return "admin"; 
  }


  @PostMapping("/users/toggle-ban")
  @Transactional
  public String toggleBanUsuario(
          @RequestParam Long userId,
          @RequestParam(required = false, defaultValue = "all") String currentFilter,
          HttpSession session) {

      User requester = (User) session.getAttribute("u");
      if (requester == null || !requester.hasRole(Role.ADMIN)) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado.");
      }

      if (requester.getId() == userId) {
          log.error("El administrador con ID {} intentó banearse a sí mismo.", requester.getId());
          return "redirect:/admin/users?filter=" + currentFilter + "&error=selfban";
      }

      User userToMod = entityManager.find(User.class, userId);
      if (userToMod == null) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
      }

      boolean nuevoEstado = !userToMod.isEnabled();
      userToMod.setEnabled(nuevoEstado);
      
      entityManager.merge(userToMod);
      log.info("El administrador {} ha cambiado el estado de baneo del usuario {} a: {}", 
                requester.getUsername(), userToMod.getUsername(), nuevoEstado);

      return "redirect:/admin/users?filter=" + currentFilter;
  }
}