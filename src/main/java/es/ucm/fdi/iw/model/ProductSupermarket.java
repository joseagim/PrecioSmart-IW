package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class ProductSupermarket {

    @EmbeddedId
    private ProductSupermarketId id = new ProductSupermarketId();

    @ManyToOne
    @MapsId("supermarketId") // Coincide con el nombre del campo en ProductSupermarketId
    private Supermarket supermarket;

    @ManyToOne
    @MapsId("productId") // Coincide con el nombre del campo en ProductSupermarketId
    private Product product;

    private LocalDate date;
    private Float price;

    @Version
    private Integer version;

    // Getters y Setters
    public ProductSupermarketId getId() { return id; }
    public void setId(ProductSupermarketId id) { this.id = id; }

    public Supermarket getSupermarket() { return supermarket; }
    public void setSupermarket(Supermarket supermarket) { this.supermarket = supermarket; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Float getPrice() { return price; }
    public void setPrice(Float price) { this.price = price; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}