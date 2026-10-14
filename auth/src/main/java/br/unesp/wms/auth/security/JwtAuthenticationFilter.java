package br.unesp.wms.auth.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Ignora a verificação de token nas rotas públicas e no Swagger repassado
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") || 
               path.equals("/swagger-ui.html") ||
               path.startsWith("/auth/login") ||
               path.startsWith("/auth/register");
    }

    @Override 
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

            String authorization = request.getHeader("Authorization");

            // Sem token, ou Header, segue a request (Caso do register)
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorization.substring(7);

            // Token inválido, não autorizado
            if (!jwtService.isValid(token)) {
                filterChain.doFilter(request, response);
                return ;
            }

            String username = jwtService.getUsername(token);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    username, null, Collections.emptyList());

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
    }
} 
