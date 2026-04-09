package com.campusstore.controller;

import com.campusstore.service.CategoryService;
import com.campusstore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home() {
        return "redirect:/catalog";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(required = false) String name,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) Boolean inStock,
                          @RequestParam(required = false) String sortBy,
                          @RequestParam(required = false) String sortDir,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "5") int size,
                          Model model) {
        model.addAttribute("products", productService.search(name, categoryId, inStock, sortBy, sortDir, page, size));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("inStock", inStock);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "catalog/index";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.getById(id));
        model.addAttribute("categories", categoryService.findAll());
        return "catalog/detail";
    }
}
