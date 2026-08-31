package com.radiocom.auth.application.dto;

import com.radiocom.auth.domain.model.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CriarUsuarioDTO {
    @NotBlank
    private String nome;

    @NotBlank
    private String login;

    @NotBlank @Email
    private String email;

    @NotBlank
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    private String senha;

    @NotNull
    private RoleUsuario role;
}
