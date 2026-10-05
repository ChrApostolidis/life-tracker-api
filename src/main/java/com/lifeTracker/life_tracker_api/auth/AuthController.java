package com.lifeTracker.life_tracker_api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LoginRateLimiter rateLimiter;
    private final SessionCookies cookies;

    public AuthController(AuthService authService, LoginRateLimiter rateLimiter, SessionCookies cookies) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.cookies = cookies;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        String caller = callerOf(http);
        long locked = rateLimiter.secondsLocked(caller);
        if (locked > 0) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(locked))
                    .build();
        }
        // No detail on failure: "wrong password" and "no password configured" look the same from outside.
        if (!authService.passwordMatches(request.password())) {
            rateLimiter.recordFailure(caller);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        rateLimiter.recordSuccess(caller);

        String client = request.client() == null ? "web" : request.client();
        String token = authService.startSession(client);
        if (client.equals("web")) {
            return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.issue(token)).build();
        }
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest http) {
        cookies.read(http).ifPresent(token -> authService.revoke(token.value()));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear()).build();
    }

    // Reaching this at all means AuthInterceptor let the request through.
    @GetMapping("/me")
    public ResponseEntity<Void> me() {
        return ResponseEntity.noContent().build();
    }

    // Behind the Cloudflare tunnel every request arrives from cloudflared, so the
    // real caller is in CF-Connecting-IP. Locally there is no tunnel and no header.
    private static String callerOf(HttpServletRequest http) {
        String forwarded = http.getHeader("CF-Connecting-IP");
        return forwarded != null && !forwarded.isBlank() ? forwarded.trim() : http.getRemoteAddr();
    }
}
