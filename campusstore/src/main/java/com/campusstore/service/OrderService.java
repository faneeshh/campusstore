package com.campusstore.service;

import com.campusstore.entity.Order;
import com.campusstore.entity.OrderItem;
import com.campusstore.entity.OrderStatus;
import com.campusstore.entity.Product;
import com.campusstore.entity.User;
import com.campusstore.repository.OrderItemRepository;
import com.campusstore.repository.OrderRepository;
import com.campusstore.repository.ProductRepository;
import com.campusstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Order createOrder(Long customerId, Map<Long, Integer> productQuantities) {
        // Validate all quantities are positive up front
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0 for product id: " + entry.getKey());
            }
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + customerId));

        // Load and validate products before modifying any state
        List<Product> products = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int qty = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));

            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            if (product.getStockQty() < qty) {
                throw new IllegalArgumentException("Insufficient stock for: " + product.getName());
            }
            products.add(product);
        }

        // Compute total and deduct stock
        BigDecimal total = BigDecimal.ZERO;
        for (Product product : products) {
            int qty = productQuantities.get(product.getId());
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
            product.setStockQty(product.getStockQty() - qty);
            productRepository.save(product);
        }

        Order order = Order.builder()
                .customer(customer)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.NEW)
                .total(total)
                .build();
        Order savedOrder = orderRepository.save(order);

        for (Product product : products) {
            int qty = productQuantities.get(product.getId());
            OrderItem item = OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .qty(qty)
                    .unitPrice(product.getPrice())
                    .build();
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    public Page<Order> getMyOrders(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findByCustomerId(customerId, pageable);
    }

    public Order getMyOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));
        if (!order.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Access denied to order: " + orderId);
        }
        return order;
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderId));

        if (order.getStatus() == OrderStatus.FULFILLED) {
            throw new IllegalStateException("Order is already fulfilled");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                Product product = item.getProduct();
                product.setStockQty(product.getStockQty() + item.getQty());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}
