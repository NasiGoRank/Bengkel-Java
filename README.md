# 🔧 Bengkel Management System

Aplikasi manajemen bengkel mobil berbasis desktop yang modern dan efisien. Dibangun menggunakan kekuatan **Java 17** untuk backend dan **JavaFX WebView** untuk menyajikan antarmuka pengguna yang responsif berbasis **HTML/CSS/JavaScript**. Aplikasi ini menggunakan database lokal **SQLite** sehingga mudah dipindahkan dan tidak memerlukan instalasi server database yang rumit.

---

## 🚀 Fitur Utama

Aplikasi ini dirancang untuk memudahkan operasional harian bengkel:

* **📊 Dashboard Interaktif**:
    * Statistik real-time untuk total customer, barang, transaksi, dan pendapatan.
    * Tabel aktivitas terbaru untuk memantau transaksi yang baru saja terjadi.
* **👥 Manajemen Pelanggan (Customer)**:
    * Tambah, edit, hapus, dan cari data pelanggan.
    * Penyimpanan data kontak dan alamat yang terstruktur.
* **📦 Manajemen Barang & Jasa (Inventory)**:
    * Pencatatan stok sparepart dan harga jasa.
    * Update stok otomatis dan monitoring ketersediaan barang.
* **💰 Transaksi & Kasir**:
    * Pencatatan servis dan penjualan sparepart.
    * Kalkulasi total bayar otomatis.
    * Riwayat transaksi lengkap.
* **📄 Laporan & Ekspor**:
    * Filter laporan transaksi berdasarkan periode tanggal.
    * **Cetak PDF Otomatis**: Generate laporan profesional siap cetak menggunakan library *iText7*.
    * Tampilan cetak (Print View) ramah printer.

---

## 🛠️ Teknologi yang Digunakan

Project ini menggabungkan teknologi Java klasik dengan web modern:

* **Bahasa Utama**: Java 21
* **UI Framework**: JavaFX 21 (WebView)
* **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
* **Database**: SQLite (via JDBC `sqlite-jdbc`)
* **Build Tool**: Apache Maven
* **PDF Library**: iText7 (`kernel`, `layout`, `io`)

---

## 📋 Prasyarat (Prerequisites)

Sebelum menjalankan aplikasi, pastikan komputer Anda memiliki:

1.  **Java Development Kit (JDK) 17** atau versi lebih baru.
2.  **Apache Maven** (terinstal dan terkonfigurasi di `PATH` sistem).
3.  **Git** (opsional, untuk clone repository).

---

## ⚙️ Cara Instalasi & Menjalankan

Ikuti langkah-langkah berikut untuk menjalankan aplikasi di komputer lokal Anda:

### 1. Clone Repository
Buka terminal atau command prompt, lalu jalankan:
```bash
git clone https://github.com/NasiGoRank/Bengkel-Java.git
cd bengkel-app
```

### 2. Compile & Build
Jalankan perintah Maven berikut untuk mengunduh dependency dan mengompilasi kode sumber:

```bash
mvn clean compile
```

### 3. Jalankan Aplikasi
Gunakan perintah berikut untuk memulai aplikasi:

```bash
mvn exec:java
```

Catatan: Saat pertama kali dijalankan, aplikasi akan otomatis membuat file database `bengkel.db` dan folder `reports/` di direktori proyek.

### 🔑 Login Default
Gunakan kredensial berikut untuk masuk ke sistem sebagai Admin:

- **Username**: admin
- **Password**: admin123

(Data login ini dibuat otomatis saat inisialisasi database)

---

## 📂 Struktur Proyek

Berikut adalah gambaran struktur folder utama proyek ini:

```
bengkel-app/
├── bengkel.db                  # File Database SQLite (Auto-generated)
├── reports/                    # Folder output Laporan PDF (Auto-generated)
├── pom.xml                     # Konfigurasi Maven & Dependencies
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── com/
        │       └── bengkel/
        │           ├── App.java                # Main Entry Point
        │           ├── JavaBridge.java         # Penghubung logika Java <-> JavaScript
        │           ├── DataStore.java          # Model Data (Legacy/Memory)
        │           ├── database/
        │           │   ├── DatabaseConnection.java  # Koneksi & Inisialisasi DB
        │           │   └── DatabaseService.java     # Logika CRUD SQL
        │           └── util/
        │               └── PDFGenerator.java        # Utility pembuatan PDF
        └── resources/
            └── web/
                └── index.html          # Tampilan Frontend (HTML/CSS/JS)
```