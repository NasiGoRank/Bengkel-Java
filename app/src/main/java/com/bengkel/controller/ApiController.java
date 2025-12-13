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
    @Autowired
    private ServiceRepository serviceRepo;

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

    // --- SERVICE ---
    @GetMapping("/services")
    public List<Service> getServices() {
        return serviceRepo.findAll();
    }

    @PostMapping("/services")
    public Service saveService(@RequestBody Service service) {
        return serviceRepo.save(service);
    }

    @DeleteMapping("/services/{id}")
    public void deleteService(@PathVariable Long id) {
        serviceRepo.deleteById(id);
    }

    // --- TRANSACTION ---
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable String id) {
        return transRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> saveTransaction(@RequestBody Transaction trans) {
        // 1. Set Nama Customer
        customerRepo.findById(trans.getCustomer().getId())
                .ifPresent(c -> trans.setCustomerName(c.getNama()));

        // LOGIKA EDIT: Jika ID ada, kembalikan stok lama dulu
        if (trans.getId() != null && !trans.getId().isEmpty()) {
            Optional<Transaction> oldTransOpt = transRepo.findById(trans.getId());
            if (oldTransOpt.isPresent()) {
                Transaction oldTrans = oldTransOpt.get();
                // Kembalikan stok barang lama
                if (oldTrans.getItems() != null) {
                    for (TransactionItem oldItem : oldTrans.getItems()) {
                        Item dbItem = itemRepo.findById(oldItem.getItem().getId()).orElse(null);
                        if (dbItem != null) {
                            dbItem.setStok(dbItem.getStok() + oldItem.getQuantity());
                            itemRepo.save(dbItem);
                        }
                    }
                }
            }
        }

        double total = 0.0;

        // 2. Hitung Total Service
        if (trans.getServices() != null) {
            for (Service s : trans.getServices()) {
                Service dbService = serviceRepo.findById(s.getId()).orElse(null);
                if (dbService != null) {
                    total += dbService.getHarga();
                }
            }
        }

        // 3. Proses Barang Baru (Hitung Total + Kurangi Stok)
        if (trans.getItems() != null) {
            for (TransactionItem ti : trans.getItems()) {
                Item dbItem = itemRepo.findById(ti.getItem().getId()).orElse(null);
                if (dbItem != null) {
                    // Cek stok (Saat edit, stok di DB sudah ditambah stok lama, jadi aman)
                    if (dbItem.getStok() < ti.getQuantity()) {
                        return ResponseEntity.badRequest()
                                .body("Stok barang " + dbItem.getNamaBarang() + " tidak mencukupi!");
                    }
                    // Kurangi stok
                    dbItem.setStok(dbItem.getStok() - ti.getQuantity());
                    itemRepo.save(dbItem);

                    // Tambah ke total
                    total += dbItem.getHarga() * ti.getQuantity();

                    // Set relasi parent
                    ti.setTransaction(trans);
                }
            }
        }

        trans.setTotalBayar(total);
        Transaction saved = transRepo.save(trans);
        return ResponseEntity.ok(saved);
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