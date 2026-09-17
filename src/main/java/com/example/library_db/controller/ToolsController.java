package com.example.library_db.controller;

import com.example.library_db.site.ToolsCatalog;
import com.example.library_db.site.VisitCounter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ToolsController {
    private final ToolsCatalog catalog;
    private final VisitCounter visits;
    public ToolsController(ToolsCatalog catalog, VisitCounter visits) { this.catalog = catalog; this.visits = visits; }

    @GetMapping("/services")
    public String catalog(Model model) {
        model.addAttribute("tools", catalog.getTools());
        return "services/catalog";
    }
    @GetMapping("/services/{slug}")
    public String tool(@PathVariable String slug, Model model) {
        var tool = catalog.find(slug);
        model.addAttribute("tool", tool);
        if (slug.equals("visits") || slug.equals("popular")) {
            var rows = visits.rows();
            model.addAttribute("visitRows", slug.equals("popular") ? rows.stream().filter(r -> r.count() > 0).limit(10).toList() : rows);
            model.addAttribute("totalVisits", visits.total());
        }
        return "services/tool";
    }
}

