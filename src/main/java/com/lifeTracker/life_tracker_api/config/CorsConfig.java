package com.lifeTracker.life_tracker_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// The one CORS allow-list for every controller. Spring rejects a request whose
// Origin is not listed with a 403 even from a non-browser client, so every new
// client (e.g. the Chrome extension) must be added to app.cors.allowed-origins.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:3000}") String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                // The registry defaults to GET/HEAD/POST; @CrossOrigin allowed every
                // mapped verb, so PATCH and DELETE must be listed explicitly.
                .allowedMethods("GET", "HEAD", "POST", "PATCH", "DELETE");
    }
}
