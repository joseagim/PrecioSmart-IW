package es.ucm.fdi.iw.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;
import jakarta.persistence.OneToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;

@Data
@Entity
@NamedQueries({
		@NamedQuery(name = "Product.EAN", query = "select obj from Product obj where :EAN = obj.EAN ")
})
public class Product {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "product")
	private Set<ProductSupermarket> vinculaciones;
    
    private String EAN;
    private String name;

  


}