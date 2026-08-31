package com.radiocom.auth.application.dto;

import com.radiocom.auth.domain.model.RoleUsuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UsuarioDTO {
    private UUID id;
    private String nome;
    private String login;
    private String email;
    private RoleUsuario role;
    private boolean ativo;
    private LocalDateTime dataCriacao;
}
