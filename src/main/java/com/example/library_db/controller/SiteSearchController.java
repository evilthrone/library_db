package com.example.library_db.controller;

import com.example.library_db.site.SiteSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SiteSearchController {
    private final SiteSearchService search;
    public SiteSearchController(SiteSearchService search) { this.search = search; }

    @GetMapping("/site-search")
    public String search(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("query", q.length() > 200 ? q.substring(0, 200) : q);
        model.addAttribute("tooLong", q.length() > 200);
        model.addAttribute("results", search.search(q));
        return "site/search";
    }
}

