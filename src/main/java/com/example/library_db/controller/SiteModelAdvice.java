package com.example.library_db.controller;

import com.example.library_db.config.SiteProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Shared presentation settings, including views rendered by exception handlers. */
@ControllerAdvice
public class SiteModelAdvice {
    private final SiteProperties site;

    public SiteModelAdvice(SiteProperties site) {
        this.site = site;
    }

    @ModelAttribute("site")
    public SiteProperties getSite() {
        return site;
    }
}
