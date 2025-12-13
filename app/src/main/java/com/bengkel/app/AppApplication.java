package com.bengkel.app;

import com.bengkel.model.Admin;
import com.bengkel.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
// Tambahkan scan package untuk memastikan semua terdeteksi (opsional tapi aman)
@ComponentScan(basePackages = "com.bengkel")
@EnableJpaRepositories("com.bengkel.repository")
@EntityScan("com.bengkel.model")
public class AppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppApplication.class, args);
	}

	// INI KODE TAMBAHAN UNTUK MEMBUAT ADMIN OTOMATIS
	@Bean
	CommandLineRunner initDatabase(AdminRepository adminRepo) {
		return args -> {
			if (adminRepo.findByUsername("admin").isEmpty()) {
				Admin admin = new Admin();
				admin.setUsername("admin");
				admin.setPassword("admin123"); // Di real app sebaiknya di-hash
				adminRepo.save(admin);
				System.out.println(">>> User 'admin' berhasil dibuat!");
			}
		};
	}
}