package es.ucm.fdi.iw.model;

import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
public class ProductCart {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Cart cart;

    @ManyToOne
    private Product product;

    private int quantity;
    private float subtotal;
}