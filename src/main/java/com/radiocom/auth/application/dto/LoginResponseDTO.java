package com.radiocom.auth.application.dto;

import com.radiocom.auth.domain.model.RoleUsuario;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private UUID usuarioId;
    private String nome;
    private String login;
    private String email;
    private RoleUsuario role;
}
