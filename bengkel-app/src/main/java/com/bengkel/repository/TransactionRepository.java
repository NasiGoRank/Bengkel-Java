package com.bengkel.repository;

import com.bengkel.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByTanggalBetweenOrderByTanggalDesc(LocalDate start, LocalDate end);

    @Query("SELECT SUM(t.totalBayar) FROM Transaction t")
    Double getTotalRevenue();

    @Query("SELECT SUM(t.totalBayar) FROM Transaction t WHERE t.tanggal BETWEEN ?1 AND ?2")
    Double getRevenueByPeriod(LocalDate start, LocalDate end);
}