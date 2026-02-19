package es.ucm.fdi.iw;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class Carrito {
    private String name;
    private double totalPrice;
    private List<Product> products;
    private LocalDate createdAt;

    public Carrito(String name) {
        this.name = name;
        this.totalPrice = 0.0;
        this.products = new ArrayList<>();
        this.createdAt = LocalDate.now();
    }

    public void addProduct(Product product) {
        products.add(product);
        totalPrice += product.getPrice() * product.getQuantity();
    }

    public String getName() {
        return name;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
    
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public List<Product> getProducts() {
        return products;
    } 
    
    public void setProducts(List<Product> products) {
        this.products = products;
        this.totalPrice = products.stream()
                                  .mapToDouble(p -> p.getPrice() * p.getQuantity())
                                  .sum();
    }
}