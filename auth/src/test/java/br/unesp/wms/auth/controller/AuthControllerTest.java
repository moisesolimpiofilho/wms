package br.unesp.wms.auth.controller;

import br.unesp.wms.auth.entity.User;
import br.unesp.wms.auth.repository.UserRepository;
import br.unesp.wms.auth.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
class AuthControllerTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AuthController(userRepository, passwordEncoder, jwtService)).build();
    }

    @Test
    void rejectsBlankRequiredCredentialsForRegisterAndLogin() throws Exception {
        for (String invalidValue : new String[]{"", "   \t"}) {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentials(invalidValue, "valid-password")))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentials("valid-user", invalidValue)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentials(invalidValue, "valid-password")))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentials("valid-user", invalidValue)))
                    .andExpect(status().isBadRequest());
        }

        for (String missingField : new String[]{"username", "password"}) {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentialsWithout(missingField)))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentialsWithout(missingField)))
                    .andExpect(status().isBadRequest());
        }
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void acceptsNonblankCredentialsForRegisterAndLogin() throws Exception {
        when(passwordEncoder.encode("secure-password")).thenReturn("encoded-password");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("alice", "secure-password")))
                .andExpect(status().isOk());

        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-password");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secure-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("alice")).thenReturn("token-value");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("alice", "secure-password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-value"));

        verify(userRepository).save(any(User.class));
    }

    private String credentials(String username, String password) throws Exception {
        Map<String, String> credentials = new LinkedHashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        return objectMapper.writeValueAsString(credentials);
    }

    private String credentialsWithout(String missingField) throws Exception {
        Map<String, String> credentials = new LinkedHashMap<>();
        if (!"username".equals(missingField)) {
            credentials.put("username", "valid-user");
        }
        if (!"password".equals(missingField)) {
            credentials.put("password", "valid-password");
        }
        return objectMapper.writeValueAsString(credentials);
    }
}
