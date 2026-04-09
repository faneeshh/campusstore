package com.campusstore.service;

import com.campusstore.entity.Category;
import com.campusstore.entity.Product;
import com.campusstore.repository.CategoryRepository;
import com.campusstore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Product create(String name, String description, BigDecimal price, int stockQty, Long categoryId) {
        validatePriceAndStock(price, stockQty);
        Category category = findCategory(categoryId);
        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stockQty(stockQty)
                .isActive(true)
                .category(category)
                .build();
        return productRepository.save(product);
    }

    public Product update(Long id, String name, String description, BigDecimal price, int stockQty, Long categoryId) {
        validatePriceAndStock(price, stockQty);
        Product product = getById(id);
        Category category = findCategory(categoryId);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQty(stockQty);
        product.setCategory(category);
        return productRepository.save(product);
    }

    public void deactivate(Long id) {
        Product product = getById(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
    }

    public Page<Product> search(String name, Long categoryId, Boolean inStock,
                                String sortBy, String sortDir, int page, int size) {
        String resolvedSortBy = (sortBy != null && sortBy.equals("price")) ? "price" : "name";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, resolvedSortBy));

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("isActive")));

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (inStock != null) {
                if (inStock) {
                    predicates.add(cb.greaterThan(root.get("stockQty"), 0));
                } else {
                    predicates.add(cb.equal(root.get("stockQty"), 0));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable);
    }

    private void validatePriceAndStock(BigDecimal price, int stockQty) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be >= 0");
        }
        if (stockQty < 0) {
            throw new IllegalArgumentException("Stock quantity must be >= 0");
        }
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + categoryId));
    }
}
