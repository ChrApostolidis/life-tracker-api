package com.lifeTracker.life_tracker_api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// The rollout is switched on through the server's .env. If one of these names
// stopped binding, setting APP_AUTH_ENABLED=true would be silently ignored and
// the API would stay open while looking locked. Pins the documented names.
class AuthEnvironmentTest {

    @Test
    void theDocumentedEnvironmentVariablesReachTheAuthSettings() {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new SystemEnvironmentPropertySource(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                Map.of(
                        "APP_AUTH_ENABLED", "true",
                        "APP_AUTH_PASSWORD_HASH", "the-hash",
                        "APP_AUTH_SERVICE_TOKEN", "the-token",
                        "APP_AUTH_COOKIE_DOMAIN", ".christosapostolidis.com",
                        "APP_AUTH_SESSION_DAYS", "30",
                        "APP_CORS_ALLOWED_ORIGINS", "https://a.example,https://b.example"
                )));
        // Resolve the way @Value does inside a Spring Boot app.
        ConfigurationPropertySources.attach(env);

        assertEquals("true", env.getProperty("app.auth.enabled"));
        assertEquals("the-hash", env.getProperty("app.auth.password-hash"));
        assertEquals("the-token", env.getProperty("app.auth.service-token"));
        assertEquals(".christosapostolidis.com", env.getProperty("app.auth.cookie-domain"));
        assertEquals("30", env.getProperty("app.auth.session-days"));
        assertEquals("https://a.example,https://b.example", env.getProperty("app.cors.allowed-origins"));
    }
}
