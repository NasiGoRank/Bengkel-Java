package com.bengkel.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String id;

    // Relasi langsung ke Customer (Best Practice JPA)
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Menyimpan snapshot nama customer saat transaksi (sesuai logika lama)
    private String customerName;

    private LocalDate tanggal;
    private String keluhan;
    private Double totalBayar;

    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}