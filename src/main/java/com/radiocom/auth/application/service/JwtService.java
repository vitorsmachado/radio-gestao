package com.radiocom.auth.application.service;

import com.radiocom.auth.domain.model.RoleUsuario;
import com.radiocom.auth.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NOME = "nome";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    /**
     * Gera um token JWT para o usuário.
     * Contém: sub (userId), role, nome, iat, exp.
     */
    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim(CLAIM_ROLE, usuario.getRole().name())
                .claim(CLAIM_NOME, usuario.getNome())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getChave())
                .compact();
    }

    public UUID extrairUserId(String token) {
        return UUID.fromString(extrairClaims(token).getSubject());
    }

    public RoleUsuario extrairRole(String token) {
        String role = extrairClaims(token).get(CLAIM_ROLE, String.class);
        return RoleUsuario.valueOf(role);
    }

    public String extrairNome(String token) {
        return extrairClaims(token).get(CLAIM_NOME, String.class);
    }

    /**
     * Valida o token — verifica assinatura e expiração.
     * @return true se válido, false caso contrário (nunca lança exceção)
     */
    public boolean isTokenValido(String token) {
        try {
            extrairClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getChave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getChave() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 requer mínimo 32 bytes — padding se necessário
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
