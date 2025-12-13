package com.bengkel.controller;

import com.bengkel.model.*;
import com.bengkel.repository.*;
import com.bengkel.util.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Izinkan akses dari mana saja (development)
public class ApiController {

    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private TransactionRepository transRepo;
    @Autowired
    private AdminRepository adminRepo;

    // --- AUTH ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        Optional<Admin> admin = adminRepo.findByUsername(payload.get("username"));
        if (admin.isPresent() && admin.get().getPassword().equals(payload.get("password"))) {
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        return ResponseEntity.status(401).body(Map.of("status", "unauthorized"));
    }

    // --- DASHBOARD ---
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return Map.of(
                "customers", customerRepo.count(),
                "items", itemRepo.count(),
                "transactions", transRepo.count(),
                "revenue", Optional.ofNullable(transRepo.getTotalRevenue()).orElse(0.0));
    }

    // --- CUSTOMER ---
    @GetMapping("/customers")
    public List<Customer> getCustomers(@RequestParam(required = false) String query) {
        if (query != null && !query.isEmpty()) {
            return customerRepo.findByNamaContainingIgnoreCase(query);
        }
        return customerRepo.findAll();
    }

    @PostMapping("/customers")
    public Customer saveCustomer(@RequestBody Customer customer) {
        return customerRepo.save(customer);
    }

    @DeleteMapping("/customers/{id}")
    public void deleteCustomer(@PathVariable String id) {
        customerRepo.deleteById(id);
    }

    // --- ITEM ---
    @GetMapping("/items")
    public List<Item> getItems() {
        return itemRepo.findAll();
    }

    @PostMapping("/items")
    public Item saveItem(@RequestBody Item item) {
        return itemRepo.save(item);
    }

    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable String id) {
        itemRepo.deleteById(id);
    }

    // --- TRANSACTION ---
    @PostMapping("/transactions")
    public Transaction saveTransaction(@RequestBody Transaction trans) {
        // Otomatis ambil nama customer jika ada ID
        customerRepo.findById(trans.getCustomer().getId()).ifPresent(c -> trans.setCustomerName(c.getNama()));
        return transRepo.save(trans);
    }

    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        return transRepo.findAll();
    }

    @DeleteMapping("/transactions/{id}")
    public void deleteTransaction(@PathVariable String id) {
        transRepo.deleteById(id);
    }

    // --- REPORT & PDF ---
    @GetMapping("/report")
    public List<Transaction> getReport(
            @RequestParam String start,
            @RequestParam String end) {
        return transRepo.findByTanggalBetweenOrderByTanggalDesc(
                LocalDate.parse(start), LocalDate.parse(end));
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam String start,
            @RequestParam String end) throws Exception {

        List<Transaction> data = transRepo.findByTanggalBetweenOrderByTanggalDesc(
                LocalDate.parse(start), LocalDate.parse(end));
        Double revenue = transRepo.getRevenueByPeriod(LocalDate.parse(start), LocalDate.parse(end));

        // Generate PDF ke Memory (ByteArray) bukan ke File fisik
        ByteArrayOutputStream out = PDFGenerator.generateToStream(data, start, end, revenue != null ? revenue : 0.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "Laporan_" + start + "_" + end + ".pdf");

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }
}