package com.campusstore.controller;

import com.campusstore.entity.OrderStatus;
import com.campusstore.service.OrderService;
import com.campusstore.service.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping("/new")
    public String newOrderForm(HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("products",
            productService.search(null, null, true, "name", "asc", 0, Integer.MAX_VALUE));
        return "orders/new";
    }

    @PostMapping
    public String createOrder(@RequestParam("productIds[]") List<Long> productIds,
                              @RequestParam("quantities[]") List<Integer> quantities,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Long userId = (Long) session.getAttribute("userId");

        Map<Long, Integer> productQuantities = new HashMap<>();
        for (int i = 0; i < productIds.size(); i++) {
            int qty = quantities.get(i);
            if (qty > 0) {
                productQuantities.put(productIds.get(i), qty);
            }
        }

        try {
            Long newOrderId = orderService.createOrder(userId, productQuantities).getId();
            return "redirect:/orders/" + newOrderId + "/confirmation";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/new";
        }
    }

    @GetMapping("/{id}/confirmation")
    public String confirmation(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Long userId = (Long) session.getAttribute("userId");
        try {
            model.addAttribute("order", orderService.getMyOrder(id, userId));
            model.addAttribute("items", orderService.getOrderItems(id));
        } catch (AccessDeniedException e) {
            return "redirect:/403";
        }
        return "orders/confirmation";
    }

    @GetMapping("/history")
    public String history(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "5") int size,
                          HttpSession session,
                          Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("orders", orderService.getMyOrders(userId, page, size));
        return "orders/history";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Long userId = (Long) session.getAttribute("userId");
        try {
            model.addAttribute("order", orderService.getMyOrder(id, userId));
            model.addAttribute("items", orderService.getOrderItems(id));
        } catch (AccessDeniedException e) {
            return "redirect:/403";
        }
        return "orders/detail";
    }
}
