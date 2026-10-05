package com.lifeTracker.life_tracker_api.auth;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public AuthWebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Login has to work without a session. So does logout: with an expired
        // session it must still clear the cookie, and it only ever ends the
        // token it is handed, so leaving it open lets no one do anything.
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/logout");
    }
}
