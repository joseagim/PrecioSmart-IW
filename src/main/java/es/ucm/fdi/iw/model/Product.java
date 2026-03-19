package es.ucm.fdi.iw.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import jakarta.persistence.OneToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;

@Data
@Entity
@NamedQueries({
        @NamedQuery(
            name = "Product.searchByName", 
            query = "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:name)"
        ),
        @NamedQuery(
            name = "Product.EAN", 
            query = "SELECT p FROM Product p WHERE p.EAN = :EAN"
        ),
        @NamedQuery(
            name = "Product.searchByNameOrEAN", 
            query = "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:param) OR p.EAN LIKE :param"
        )
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "product")
    private Set<ProductSupermarket> vinculaciones;

    private String EAN;
    private String name;
    private String brand;
    private String cantidad;
    private String imageURL;
}