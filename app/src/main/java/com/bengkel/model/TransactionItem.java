package com.bengkel.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class TransactionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    private Integer quantity;

    @JsonIgnore // Mencegah looping JSON saat fetch
    @ManyToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
}