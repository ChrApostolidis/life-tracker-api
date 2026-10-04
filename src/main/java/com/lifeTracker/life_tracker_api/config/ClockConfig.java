package com.lifeTracker.life_tracker_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Injected rather than calling Instant.now() directly, so tests can move time
// forward to check session expiry and login lockouts.
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
