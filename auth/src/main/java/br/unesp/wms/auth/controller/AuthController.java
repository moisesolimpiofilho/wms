package br.unesp.wms.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.unesp.wms.auth.entity.User;
import br.unesp.wms.auth.repository.UserRepository;
import br.unesp.wms.auth.security.JwtService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping("/auth")
@RequiredArgsConstructor 
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public void register(@RequestBody Map<String, String> request) {
        
        User user = new User();
        user.setUsername(request.get("username"));
        user.setPassword(
            passwordEncoder.encode(request.get("password"))
        );

        userRepository.save(user);
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        
        User user = userRepository
            .findByUsername(request.get("username"))
            .orElseThrow(null);

        boolean passwordOk = passwordEncoder.matches(request.get("password"), user.getPassword());

        if (user == null || !passwordOk) {
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
}


