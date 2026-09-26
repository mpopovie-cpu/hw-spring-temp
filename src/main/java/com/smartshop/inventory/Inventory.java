package com.smartshop.inventory;

import com.smartshop.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // владеющая сторона, FK product_id уникален
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    protected Inventory() {
    }

    public Inventory(Integer quantity, Integer reservedQuantity, LocalDateTime updatedAt, Product product) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity не может быть отрицательным");
        }
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("reservedQuantity не может быть отрицательным");
        }
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.updatedAt = updatedAt;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity не может быть отрицательным");
        }
        this.quantity = quantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        if (reservedQuantity < 0) {
            throw new IllegalArgumentException("reservedQuantity не может быть отрицательным");
        }
        this.reservedQuantity = reservedQuantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Inventory)) {
            return false;
        }
        Inventory other = (Inventory) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Inventory{id=" + id + ", quantity=" + quantity + ", reservedQuantity=" + reservedQuantity + "}";
    }
}