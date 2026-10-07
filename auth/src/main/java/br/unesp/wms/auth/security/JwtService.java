package br.unesp.wms.auth.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

        this.secretKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret));

        this.expiration = expiration;
    }

    /**
     * Gera o token adicionando as informações
     * e compactando em string
     * 
     * @param username
     * @return
     */
    public String generateToken(String username) {
        Date now = new Date();

        Date expirationDate = new Date(
            now.getTime() + expiration
        );

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(secretKey)
            .compact();
    }

    public String getUsername(String token) {
        return getClaims(token)
                .getSubject();
    }

    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Faz a validação do token
     * 
     * @param token
     * @return
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
