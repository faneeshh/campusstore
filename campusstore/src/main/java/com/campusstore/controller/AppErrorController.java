package com.campusstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppErrorController {

    @GetMapping("/403")
    public String forbidden(Model model) {
        model.addAttribute("message", "Forbidden");
        return "error/403";
    }

    @GetMapping("/404")
    public String notFound() {
        return "error/404";
    }

    @GetMapping("/error")
    public String generalError() {
        return "error/general";
    }
}
