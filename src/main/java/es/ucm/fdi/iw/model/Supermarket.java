package es.ucm.fdi.iw.model;

import lombok.Data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.NamedQueries;

@Data
@Entity
@NamedQueries({
        @NamedQuery(
            name="Supermarket.searchByName", 
            query="SELECT s FROM Supermarket s WHERE s.name = :name"
        ),
        @NamedQuery(
            name = "Supermarket.totalNum", 
            query = "SELECT COUNT(s) FROM Supermarket s"
        )
})
public class Supermarket {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    private String name;

    private String info;

    static final String[] NAMES = {"Mercadona", "Carrefour", "Lidl", "Alcampo", "Dia"};
}