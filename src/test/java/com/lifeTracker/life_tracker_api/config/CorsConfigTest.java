package com.lifeTracker.life_tracker_api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.cors.allowed-origins=" + CorsConfigTest.WEB + "," + CorsConfigTest.EXTENSION)
class CorsConfigTest {

    static final String WEB = "https://web.example";
    static final String EXTENSION = "chrome-extension://abcdefghijklmnopabcdefghijklmnop";
    private static final String MISSING_TASK = "/api/tasks/00000000-0000-0000-0000-000000000000";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // The regression this config could cause silently: the registry only allows
    // GET/HEAD/POST by default, which would break every edit and delete in the web app.
    @Test
    void webOriginMayPreflightPatchAndDelete() throws Exception {
        for (String method : new String[] {"PATCH", "DELETE"}) {
            mvc.perform(options(MISSING_TASK)
                            .header("Origin", WEB)
                            .header("Access-Control-Request-Method", method))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", WEB));
        }
    }

    // The production bug: Spring answered the extension with 403 before the handler ran.
    @Test
    void extensionOriginReachesTheHandler() throws Exception {
        mvc.perform(get(MISSING_TASK).header("Origin", EXTENSION))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Access-Control-Allow-Origin", EXTENSION));
    }

    @Test
    void unlistedOriginIsRefused() throws Exception {
        mvc.perform(get(MISSING_TASK).header("Origin", "https://evil.example"))
                .andExpect(status().isForbidden());
    }
}
