package com.bengkel.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "item")
public class Item {
    @Id
    private String id;
    private String namaBarang;
    private Integer stok;
    private Double harga;

    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}