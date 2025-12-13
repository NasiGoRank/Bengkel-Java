package com.bengkel.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String namaService;
    private Double harga;
}