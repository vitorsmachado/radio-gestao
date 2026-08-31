package com.radiocom.auth.unit.domain.model;

import com.radiocom.auth.domain.model.RoleUsuario;
import com.radiocom.auth.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario - Testes de Domínio")
class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .nome("Vitor Silva")
                .email("vitor@radiogestao.com.br")
                .login("vitor.silva")
                .senhaHash("hash-fake")
                .role(RoleUsuario.TECNICO)
                .build();
    }

    @Test
    @DisplayName("novo usuário deve nascer ativo por padrão")
    void novoUsuario_deveNascerAtivo() {
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("desativar deve marcar usuário como inativo")
    void desativar_deveMarcarComoInativo() {
        usuario.desativar();

        assertThat(usuario.isAtivo()).isFalse();
    }

    @Test
    @DisplayName("ativar deve marcar usuário como ativo")
    void ativar_deveMarcarComoAtivo() {
        usuario.desativar();

        usuario.ativar();

        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("isAdmin deve retornar true apenas quando role é ADMIN")
    void isAdmin_deveRetornarTrueApenasParaAdmin() {
        assertThat(usuario.isAdmin()).isFalse();

        usuario.setRole(RoleUsuario.ADMIN);

        assertThat(usuario.isAdmin()).isTrue();
    }
}
