package br.unesp.wms.auth.config;

import br.unesp.wms.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.ProtectedController.class)
@Import({SecurityConfig.class, SecurityConfigTest.ProtectedController.class})
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @Test
    void returnsForbiddenWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsForbiddenWhenAuthorizationTokenIsInvalid() throws Exception {
        when(jwtService.isValid("invalid-token")).thenReturn(false);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsRequestWithValidAuthorizationToken() throws Exception {
        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.getUsername("valid-token")).thenReturn("user");

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @RestController
    static class ProtectedController {
        @GetMapping(value = "/api/test", produces = MediaType.TEXT_PLAIN_VALUE)
        String protectedEndpoint() {
            return "ok";
        }
    }
}
