package es.ucm.fdi.iw.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Topic;
import es.ucm.fdi.iw.model.Lorem;
import es.ucm.fdi.iw.model.Message;
import es.ucm.fdi.iw.model.Request;
import es.ucm.fdi.iw.model.RequestStatus;
import es.ucm.fdi.iw.model.Transferable;
import es.ucm.fdi.iw.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
  private PasswordEncoder passwordEncoder;

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
          .setParameter("status", es.ucm.fdi.iw.model.RequestStatus.PENDING)
          .getResultList();

    } else if (type.equals("accepted")) {

      requests = entityManager.createNamedQuery("findByStatus", Request.class)
          .setParameter("status", es.ucm.fdi.iw.model.RequestStatus.APPROVED)
          .getResultList();

    } else if (type.equals("rejected")) {

      requests = entityManager.createNamedQuery("findByStatus", Request.class)
          .setParameter("status", es.ucm.fdi.iw.model.RequestStatus.REJECTED)
          .getResultList();

    } else {
      // manejar error, type no es pending, ni accepted ni rejected
    }

    model.addAttribute("requests", requests);
    model.addAttribute("type", type);

    return "admin";
  }

  @PostMapping("/mod/accept")
  public ResponseEntity acceptRequest(Model model, @RequestParam Long id) {
    if (id == null) {
      return ResponseEntity.badRequest().body("El ID de la solicitud es requerido");
    }

    Request request = entityManager.find(Request.class, id);
    if (request == null) {
      return ResponseEntity.badRequest().body("No se encontró la solicitud con el ID proporcionado");
    }
    if (request.getStatus() != RequestStatus.PENDING) {
      return ResponseEntity.badRequest().body("La solicitud ya ha sido procesada");
    }

    request.setStatus(RequestStatus.APPROVED);
    entityManager.merge(request);

    return null;
  }

  @PostMapping("/mod/reject")
  public String rejectRequest(Model model, @RequestParam Long id) {
    return "";
  }

}
