package es.ucm.fdi.iw.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String EAN;
    private String info;
    
  
    private LocalDate date;
    
    private Float price;
    private Boolean approved;

    @ManyToOne
    private User user; // Generará la columna 'user_id' automáticamente

    @Version
    private Long version;

    // --- Getters y Setters ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEan() { return EAN; }
    public void setEan(String EAN) { this.EAN = EAN; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}