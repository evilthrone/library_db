package com.example.library_db.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import com.example.library_db.site.SitePageCatalog;

@Controller
public class SiteController {
    private final SitePageCatalog pages;

    public SiteController(SitePageCatalog pages) {
        this.pages = pages;
    }

    @GetMapping({"/about", "/rules", "/membership", "/departments", "/library-services",
            "/achievements", "/resources", "/faq", "/contacts"})
    public String informationPage(HttpServletRequest request, Model model) {
        model.addAttribute("page", pages.findByPath(request.getRequestURI().substring(request.getContextPath().length())));
        return "site/information";
    }

    @GetMapping("/")
    public String home() {
        return "site/home";
    }

    @GetMapping("/app")
    public String app() {
        return "app-menu";
    }
}
