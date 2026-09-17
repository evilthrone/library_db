package com.example.library_db.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "site")
public record SiteProperties(
        String name,
        String tagline,
        String city,
        String address,
        String openingHours,
        String phone,
        String email,
        String developer,
        double latitude,
        double longitude,
        boolean demoLocation
) {
}
