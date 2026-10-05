package com.lifeTracker.life_tracker_api.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// The whole flow over HTTP with enforcement switched on, as it will run once
// AUTH_PLAN.md's rollout reaches its last step.
@SpringBootTest
class AuthApiTest {

    private static final String PASSWORD = "correct horse battery staple";
    private static final String WEB = "https://web.example";
    private static final String SERVICE_TOKEN = "service-secret-for-tests";
    private static final String GUARDED = "/api/tasks/overdue";

    @DynamicPropertySource
    static void auth(DynamicPropertyRegistry registry) {
        registry.add("app.auth.enabled", () -> "true");
        registry.add("app.auth.password-hash", () -> new BCryptPasswordEncoder(4).encode(PASSWORD));
        registry.add("app.auth.service-token", () -> SERVICE_TOKEN);
        registry.add("app.cors.allowed-origins", () -> WEB);
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // Each test logs in from its own address so lockouts never leak between tests.
    private MockHttpServletRequestBuilder login(String password, String client) {
        return loginFrom(UUID.randomUUID().toString(), password, client);
    }

    // The address is set exactly once: MockMvc's header() adds a second value
    // rather than replacing the first, and the server reads the first.
    private MockHttpServletRequestBuilder loginFrom(String caller, String password, String client) {
        String body = client == null
                ? "{\"password\":\"" + password + "\"}"
                : "{\"password\":\"" + password + "\",\"client\":\"" + client + "\"}";
        return post("/api/auth/login")
                .header("CF-Connecting-IP", caller)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
    }

    private String cookieFrom(MvcResult result) {
        String header = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(header, "login should set the session cookie");
        return header.substring((SessionCookies.NAME + "=").length(), header.indexOf(';'));
    }

    @Test
    void noSessionMeans401() throws Exception {
        mvc.perform(get(GUARDED)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void the401StillCarriesCorsHeadersSoTheWebAppCanReadIt() throws Exception {
        // Without these the browser reports a network error instead of a 401,
        // and the web app could not send you to /login.
        mvc.perform(get(GUARDED).header("Origin", WEB))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", WEB))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void preflightsPassWithoutCredentials() throws Exception {
        mvc.perform(options(GUARDED)
                        .header("Origin", WEB)
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk());
    }

    @Test
    void webLoginSetsASafeCookieThatOpensTheApi() throws Exception {
        MvcResult result = mvc.perform(login(PASSWORD, "web"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("Secure")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=2592000"))) // 30 days
                .andExpect(content().string("")) // the token never appears in the body
                .andReturn();
        Cookie session = new Cookie(SessionCookies.NAME, cookieFrom(result));

        mvc.perform(get("/api/auth/me").cookie(session)).andExpect(status().isNoContent());
        mvc.perform(get(GUARDED).cookie(session)).andExpect(status().isOk());
    }

    @Test
    void anAbsentClientMeansWeb() throws Exception {
        mvc.perform(login(PASSWORD, null))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    void extensionLoginReturnsABearerTokenAndNoCookie() throws Exception {
        MvcResult result = mvc.perform(login(PASSWORD, "extension"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();
        String token = result.getResponse().getContentAsString().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mvc.perform(get(GUARDED).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void wrongPasswordIs401WithNoCookie() throws Exception {
        mvc.perform(login("wrong", "web"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void anUnknownClientIsRejected() throws Exception {
        mvc.perform(login(PASSWORD, "toaster")).andExpect(status().isBadRequest());
    }

    @Test
    void fiveWrongGuessesLockEvenTheRightPasswordOut() throws Exception {
        String ip = "203.0.113.7";
        for (int i = 0; i < LoginRateLimiter.MAX_FAILURES; i++) {
            mvc.perform(loginFrom(ip, "wrong", "web"))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(loginFrom(ip, PASSWORD, "web").header("Origin", WEB))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                // Otherwise the login page's script cannot read how long to wait.
                .andExpect(header().string("Access-Control-Expose-Headers", containsString("Retry-After")))
                .andExpect(header().doesNotExist("Set-Cookie"));
        // Someone else is unaffected.
        mvc.perform(login(PASSWORD, "web")).andExpect(status().isNoContent());
    }

    @Test
    void logoutClearsTheCookieAndKillsTheSession() throws Exception {
        Cookie session = new Cookie(SessionCookies.NAME, cookieFrom(mvc.perform(login(PASSWORD, "web")).andReturn()));

        mvc.perform(post("/api/auth/logout").cookie(session))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        mvc.perform(get(GUARDED).cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutWorksEvenWithADeadSession() throws Exception {
        // An expired cookie must still be clearable.
        mvc.perform(post("/api/auth/logout").cookie(new Cookie(SessionCookies.NAME, "long-gone")))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }

    @Test
    void theServiceTokenOpensTheApiForTheBot() throws Exception {
        mvc.perform(get(GUARDED).header("Authorization", "Bearer " + SERVICE_TOKEN)).andExpect(status().isOk());
        mvc.perform(get(GUARDED).header("Authorization", "Bearer " + SERVICE_TOKEN + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aForgedCookieIsRejected() throws Exception {
        mvc.perform(get(GUARDED).cookie(new Cookie(SessionCookies.NAME, "forged")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
}
