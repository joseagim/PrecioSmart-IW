package es.ucm.fdi.iw.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import jakarta.persistence.OneToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;

@Entity
@NamedQueries({
		@NamedQuery(name = "Product.EAN", query = "select obj from Product obj where :EAN = obj.EAN ")
})
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Version
    private Integer version;

    @OneToMany(mappedBy = "product")
	private Set<ProductSupermarket> vinculaciones;
    
    private String EAN;
    private String name;

    public Product() {
        
    }

    public Product(String EAN, String name) {
        this.EAN = EAN;
        this.name = name;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    public String getEAN() {
        return EAN;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}