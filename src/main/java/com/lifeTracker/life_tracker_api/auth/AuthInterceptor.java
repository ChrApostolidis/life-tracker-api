package com.lifeTracker.life_tracker_api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Requires a live session or the service token on every /api request.
 *
 * An interceptor rather than a servlet filter on purpose: Spring adds its CORS
 * headers before interceptors run, so a 401 from here still reaches the browser
 * readable. From a filter it would surface as an opaque network error, and the
 * web app could not tell "logged out" from "server down".
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final boolean enabled;
    private final AuthService authService;
    private final SessionCookies cookies;

    public AuthInterceptor(
            // Off until every client can log in; see AUTH_PLAN.md's rollout.
            @Value("${app.auth.enabled:false}") boolean enabled,
            AuthService authService,
            SessionCookies cookies
    ) {
        // Turning enforcement on with no usable password would lock everyone
        // out, so refuse to start instead of failing quietly at the first request.
        if (enabled && !authService.hasUsablePasswordHash()) {
            throw new IllegalStateException(
                    "app.auth.enabled is true but app.auth.password-hash is not a BCrypt hash. "
                            + "Set it before enabling auth, or nobody can log in.");
        }
        this.enabled = enabled;
        this.authService = authService;
        this.cookies = cookies;
        // Visible in `docker logs`, so turning the switch on can be confirmed.
        log.info("API authentication is {}", enabled ? "ENFORCED" : "OFF (app.auth.enabled=false)");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Preflights carry no credentials by design; CORS already vetted the origin.
        if (!enabled || CorsUtils.isPreFlightRequest(request)) return true;

        Optional<SessionCookies.PresentedToken> token = cookies.read(request);
        if (token.isPresent()) {
            String value = token.get().value();
            if (authService.isServiceToken(value)) return true;

            Optional<AuthService.Authenticated> auth = authService.authenticate(value);
            if (auth.isPresent()) {
                // The server pushed the expiry back, so push the cookie's back with it.
                if (auth.get().renewed() && token.get().fromCookie()) {
                    response.addHeader(HttpHeaders.SET_COOKIE, cookies.issue(value));
                }
                return true;
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
