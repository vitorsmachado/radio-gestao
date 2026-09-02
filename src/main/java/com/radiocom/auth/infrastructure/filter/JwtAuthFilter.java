package com.radiocom.auth.infrastructure.filter;

import com.radiocom.auth.application.service.JwtService;
import com.radiocom.auth.domain.model.RoleUsuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filtro JWT — executa uma vez por requisição.
 *
 * Fluxo:
 * 1. Extrai "Bearer <token>" do header Authorization
 * 2. Valida o token via JwtService
 * 3. Extrai userId e role do token
 * 4. Injeta Authentication no SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        if (!jwtService.isTokenValido(token)) {
            log.debug("Token inválido na requisição para {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        UUID userId = jwtService.extrairUserId(token);
        RoleUsuario role = jwtService.extrairRole(token);
        String nome = jwtService.extrairNome(token);

        var authority = new SimpleGrantedAuthority("ROLE_" + role.name());
        var authentication = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                List.of(authority)
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("Usuário autenticado: {} ({}) → {}", nome, userId, role);

        filterChain.doFilter(request, response);
    }
}
