package com.radiocom.auth.unit.application.service;

import com.radiocom.auth.application.service.JwtService;
import com.radiocom.auth.domain.model.RoleUsuario;
import com.radiocom.auth.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService - Testes Unitários")
class JwtServiceTest {

    private JwtService jwtService;
    private Usuario usuario;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "chave-secreta-de-teste-com-pelo-menos-32-bytes");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3600000L);

        Field idField = Usuario.class.getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);

        usuario = Usuario.builder()
                .nome("Vitor Silva")
                .email("vitor@radiogestao.com.br")
                .login("vitor.silva")
                .senhaHash("hash-fake")
                .role(RoleUsuario.TECNICO)
                .build();
        idField.set(usuario, UUID.randomUUID());
    }

    @Test
    @DisplayName("gerarToken e extrairUserId devem ser consistentes")
    void gerarTokenEExtrairUserId_devemSerConsistentes() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairUserId(token)).isEqualTo(usuario.getId());
    }

    @Test
    @DisplayName("extrairRole deve retornar a role do usuário")
    void extrairRole_deveRetornarRoleDoUsuario() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairRole(token)).isEqualTo(RoleUsuario.TECNICO);
    }

    @Test
    @DisplayName("extrairNome deve retornar o nome do usuário")
    void extrairNome_deveRetornarNomeDoUsuario() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.extrairNome(token)).isEqualTo("Vitor Silva");
    }

    @Test
    @DisplayName("isTokenValido deve retornar true para token recém-gerado")
    void isTokenValido_deveRetornarTrueParaTokenValido() {
        String token = jwtService.gerarToken(usuario);

        assertThat(jwtService.isTokenValido(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValido deve retornar false para token malformado")
    void isTokenValido_deveRetornarFalseParaTokenMalformado() {
        assertThat(jwtService.isTokenValido("token-invalido")).isFalse();
    }

    @Test
    @DisplayName("isTokenValido deve retornar false para token expirado")
    void isTokenValido_deveRetornarFalseParaTokenExpirado() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);

        String tokenExpirado = jwtService.gerarToken(usuario);

        assertThat(jwtService.isTokenValido(tokenExpirado)).isFalse();
    }
}
