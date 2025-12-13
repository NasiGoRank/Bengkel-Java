package com.bengkel.repository;

import com.bengkel.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    List<Customer> findByNamaContainingIgnoreCase(String nama);
}