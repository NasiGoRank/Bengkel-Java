# Bengkel Management System (Spring Boot Edition)

Aplikasi manajemen bengkel mobil modern yang telah dimigrasi menjadi **Web Application** berbasis **Spring Boot**.
Aplikasi ini menggunakan arsitektur **MVC** dan **REST API**, dengan antarmuka pengguna responsif berbasis **HTML, CSS, dan JavaScript** yang terintegrasi dengan **Thymeleaf**.

Database menggunakan **H2 Database (file-based)** yang ringan, tanpa memerlukan instalasi server database terpisah.

---

## Fitur Utama

Aplikasi ini dirancang untuk mendukung operasional harian bengkel secara efisien:

### Dashboard Interaktif

* Statistik real-time:

  * Total customer
  * Total barang
  * Total transaksi
  * Total pendapatan
* Ringkasan aktivitas terbaru dalam bentuk tabel/grafik.

### Manajemen Pelanggan (Customer)

* CRUD data pelanggan melalui REST API.
* Pencarian pelanggan secara real-time.

### Manajemen Barang (Inventory)

* Monitoring stok sparepart.
* Indikator status stok:

  * Tersedia
  * Habis

### Transaksi & Kasir

* Pencatatan servis dan penjualan.
* Perhitungan total otomatis.
* Penghapusan riwayat transaksi.

### Laporan & Ekspor

* Filter laporan berdasarkan rentang tanggal.
* Generate laporan PDF otomatis menggunakan **Apache PDFBox**.
* Tampilan cetak (print view) untuk printer.

---

## Teknologi yang Digunakan

* **Backend Framework**: Spring Boot 3.4.1
* **Bahasa Pemrograman**: Java 21
* **Template Engine**: Thymeleaf
* **Frontend**: HTML5, CSS3, JavaScript (Fetch API)
* **Database**: H2 Database (File Mode)
* **PDF Library**: Apache PDFBox 2.0.29
* **Build Tool**: Apache Maven

---

## Prasyarat

Pastikan environment sudah memenuhi kebutuhan berikut:

1. Java Development Kit (JDK) 21
2. Maven (opsional, karena proyek sudah menyertakan Maven Wrapper)

---

## Cara Instalasi & Menjalankan Aplikasi

Ikuti langkah berikut untuk menjalankan aplikasi di localhost.

### 1. Buka Project

Masuk ke direktori root project:

```
cd app/
```

### 2. Jalankan Aplikasi

Gunakan Maven Wrapper yang sudah disediakan.

**Windows**

```cmd
mvnw spring-boot:run
```

**Mac / Linux**

```bash
./mvnw spring-boot:run
```

Tunggu hingga muncul log:

```
Started AppApplication in ... seconds
```

### 3. Akses Aplikasi

Buka browser dan kunjungi:

```
http://localhost:8080
```

---

## Login Default

Saat aplikasi pertama kali dijalankan, sistem akan otomatis membuat akun admin melalui data seeder.

* **Username**: `admin`
* **Password**: `admin123`

---

## Struktur Proyek

Struktur folder mengikuti standar Spring Boot:

```
bengkel-app/
├── bengkelDB.mv.db              # File database H2 (auto-generated)
├── mvnw
├── mvnw.cmd
├── pom.xml                      # Dependency Maven
└── src/
    └── main/
        ├── java/com/bengkel/
        │   ├── app/
        │   │   └── AppApplication.java
        │   │       # Main class + data seeder
        │   ├── controller/
        │   │   ├── ApiController.java
        │   │   │   # REST API endpoints
        │   │   └── WebController.java
        │   │       # Routing halaman web
        │   ├── model/
        │   │   # Entity JPA (Customer, Item, Transaction)
        │   ├── repository/
        │   │   # Interface akses database
        │   └── util/
        │       └── PDFGenerator.java
        │           # Logic pembuatan PDF
        └── resources/
            ├── application.properties
            │   # Konfigurasi server & database
            ├── static/
            │   ├── css/
            │   │   └── style.css
            │   └── js/
            │       └── script.js
            └── templates/
                └── index.html
```
