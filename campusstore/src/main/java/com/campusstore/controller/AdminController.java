package com.campusstore.controller;

import com.campusstore.entity.OrderStatus;
import com.campusstore.service.CategoryService;
import com.campusstore.service.OrderService;
import com.campusstore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping
    public String admin() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("products", productService.search(null, null, null, "name", "asc", 0, 10));
        model.addAttribute("orders", orderService.getAllOrders(0, 10));
        return "admin/dashboard";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm() {
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name) {
        categoryService.create(name);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String createProduct(@RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal price,
                                @RequestParam int stockQty,
                                @RequestParam Long categoryId,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.create(name, description, price, stockQty, categoryId);
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/products/new";
        }
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product-form";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal price,
                                @RequestParam int stockQty,
                                @RequestParam Long categoryId) {
        productService.update(id, name, description, price, stockQty, categoryId);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/products/{id}/deactivate")
    public String deactivateProduct(@PathVariable Long id) {
        productService.deactivate(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/orders")
    public String allOrders(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        model.addAttribute("orders", orderService.getAllOrders(page, size));
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status);
            orderService.updateStatus(id, newStatus);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders";
    }
}
