package com.example.library_db.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties(SiteProperties.class)
@PropertySource(value = "classpath:site.properties", encoding = "UTF-8")
public class SiteConfig {
}
