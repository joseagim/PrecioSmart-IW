package es.ucm.fdi.iw.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@NamedQueries({
        @NamedQuery(name = "ProductSupermarket.findByProduct", query = "SELECT ps FROM ProductSupermarket ps WHERE ps.product.id = :productId ORDER BY ps.date DESC"),
        @NamedQuery(name = "ProductSupermarket.findBySupermarket", query = "SELECT ps FROM ProductSupermarket ps WHERE ps.supermarket.id = :supermarketId ORDER BY ps.date DESC"),
        @NamedQuery(name = "ProductSupermarket.findProductSupermarket", query = "SELECT ps FROM ProductSupermarket ps WHERE ps.supermarket.id = :supermarketId AND ps.product.id = :productId ORDER BY ps.date DESC")
})
public class ProductSupermarket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Supermarket supermarket;

    @ManyToOne
    private Product product;

    private LocalDateTime date;
    private float price;
}