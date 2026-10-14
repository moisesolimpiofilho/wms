package br.unesp.wms.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.unesp.wms.auth.entity.User;
import br.unesp.wms.auth.repository.UserRepository;
import br.unesp.wms.auth.security.JwtService;
import br.unesp.wms.dto.AuthRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController 
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para registro de novos usuários e geração de token JWT")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário na base de dados encriptando a senha enviada")
    public ResponseEntity<Void> register(@RequestBody AuthRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return ResponseEntity.badRequest().build();
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Valida as credenciais do usuário e retorna o token JWT para autenticação")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return ResponseEntity.badRequest().build();
        }

        User user = userRepository
            .findByUsername(request.getUsername())
            .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                    "status", 401,
                    "message", "Usuário ou senha inválidos"
                ));
        }

        String token = jwtService.generateToken(user.getUsername());
        
        return ResponseEntity
            .ok()
            .body(Map.of(
                "token", token
            ));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}