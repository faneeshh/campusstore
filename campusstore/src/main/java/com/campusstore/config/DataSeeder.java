package com.campusstore.config;

import com.campusstore.entity.*;
import com.campusstore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Admin account
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@example.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin@1234"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        // Categories
        if (categoryRepository.count() == 0) {
            Category books = new Category(); books.setName("Books");
            Category electronics = new Category(); electronics.setName("Electronics");
            Category stationery = new Category(); stationery.setName("Stationery");
            categoryRepository.save(books);
            categoryRepository.save(electronics);
            categoryRepository.save(stationery);

            // Products (at least 6 active for pagination demo)
            saveProduct("Java Programming", "Learn Java from scratch", new BigDecimal("49.99"), 20, books);
            saveProduct("Data Structures", "Algorithms and data structures", new BigDecimal("39.99"), 15, books);
            saveProduct("USB-C Hub", "7-in-1 USB-C hub", new BigDecimal("29.99"), 10, electronics);
            saveProduct("Wireless Mouse", "Ergonomic wireless mouse", new BigDecimal("24.99"), 8, electronics);
            saveProduct("Notebook Set", "Pack of 3 ruled notebooks", new BigDecimal("9.99"), 50, stationery);
            saveProduct("Highlighter Pack", "6 colour highlighters", new BigDecimal("5.99"), 40, stationery);
            saveProduct("Mechanical Keyboard", "Compact TKL keyboard", new BigDecimal("79.99"), 5, electronics);
            saveProduct("Graph Paper Pad", "A4 graph paper, 50 sheets", new BigDecimal("4.99"), 30, stationery);
        }
    }

    private void saveProduct(String name, String description, BigDecimal price, int stock, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQty(stock);
        p.setIsActive(true);
        p.setCategory(category);
        productRepository.save(p);
    }
}