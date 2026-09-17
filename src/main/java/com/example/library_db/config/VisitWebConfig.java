package com.example.library_db.config;

import com.example.library_db.site.VisitCounter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class VisitWebConfig implements WebMvcConfigurer {
    private final VisitCounter counter;
    public VisitWebConfig(VisitCounter counter) { this.counter = counter; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                if ("GET".equals(request.getMethod()) && counter.tracks(path) && request.getAttribute("pageVisitCount") == null) {
                    request.setAttribute("pageVisitCount", counter.recordVisit(path));
                }
                return true;
            }
        });
    }
}

