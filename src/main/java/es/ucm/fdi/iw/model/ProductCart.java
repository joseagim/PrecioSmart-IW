package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
public class ProductCart {

    @EmbeddedId
    private ProductCartId id;

    @ManyToOne
    @MapsId("cartId")
    private Cart cart;

    @ManyToOne
    @MapsId("productId")
    private Product product;

    private Integer quantity;
    private Float subtotal;

    @Version
    private Integer version;

    public ProductCart() {
        
    }

    // Getters and Setters

    public ProductCartId getId() {
        return id;
    }

    public void setId(ProductCartId id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Float subtotal) {
        this.subtotal = subtotal;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}