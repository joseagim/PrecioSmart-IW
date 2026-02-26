package es.ucm.fdi.iw.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ProductSupermarketId implements Serializable {

    private Integer supermarketId;
    private Integer productId;

    // Constructor vacío obligatorio
    public ProductSupermarketId() {}

    public ProductSupermarketId(Integer supermarketId, Integer productId) {
        this.supermarketId = supermarketId;
        this.productId = productId;
    }

    // Getters y Setters
    public Integer getSupermarketId() { return supermarketId; }
    public void setSupermarketId(Integer supermarketId) { this.supermarketId = supermarketId; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    // equals() y hashCode() son OBLIGATORIOS para llaves compuestas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductSupermarketId that = (ProductSupermarketId) o;
        return Objects.equals(supermarketId, that.supermarketId) && 
               Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supermarketId, productId);
    }
}
