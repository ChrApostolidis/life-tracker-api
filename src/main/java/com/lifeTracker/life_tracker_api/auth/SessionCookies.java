package com.lifeTracker.life_tracker_api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Where a request's session token lives, and the cookie the web app keeps it
 * in. httpOnly so no script on the page can read it; SameSite=Lax so another
 * site cannot make the browser send it along with a forged request.
 */
@Component
public class SessionCookies {

    static final String NAME = "lt_session";

    /** A token and whether it arrived in the cookie (web) or a header (everything else). */
    public record PresentedToken(String value, boolean fromCookie) {}

    private final String domain;
    private final boolean secure;
    private final Duration lifetime;

    public SessionCookies(
            // '.christosapostolidis.com' in production, so the web app's host
            // sees the cookie too; blank locally, where localhost covers both ports.
            @Value("${app.auth.cookie-domain:}") String domain,
            @Value("${app.auth.cookie-secure:true}") boolean secure,
            @Value("${app.auth.session-days:30}") int sessionDays
    ) {
        this.domain = domain.trim();
        this.secure = secure;
        this.lifetime = Duration.ofDays(sessionDays);
    }

    /** A Bearer header wins over the cookie, so an explicit token is never ignored. */
    public Optional<PresentedToken> read(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ") && header.length() > 7) {
            return Optional.of(new PresentedToken(header.substring(7).trim(), false));
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> NAME.equals(c.getName()) && !c.getValue().isBlank())
                .findFirst()
                .map(c -> new PresentedToken(c.getValue(), true));
    }

    public String issue(String token) {
        return build(token, lifetime);
    }

    public String clear() {
        return build("", Duration.ZERO);
    }

    private String build(String value, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge);
        if (!domain.isEmpty()) cookie.domain(domain);
        return cookie.build().toString();
    }
}
