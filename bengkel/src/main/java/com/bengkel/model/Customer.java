package com.bengkel.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    private String id;
    private String nama;
    private String noTelp;
    private String alamat;

    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}