package com.bengkel.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String customerName;
    private LocalDate tanggal;
    private String keluhan;
    private Double totalBayar;

    // FITUR BARU: List Service yang dipilih
    @ManyToMany
    @JoinTable(name = "transaction_services", joinColumns = @JoinColumn(name = "transaction_id"), inverseJoinColumns = @JoinColumn(name = "service_id"))
    private List<Service> services;

    // FITUR BARU: List Barang dengan quantity
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    private List<TransactionItem> items;

    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}