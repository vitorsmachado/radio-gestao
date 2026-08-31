package com.radiocom.auth.domain.model;

import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "usuarios", indexes = {
        @Index(name = "idx_usuario_email", columnList = "email", unique = true),
        @Index(name = "idx_usuario_login", columnList = "login", unique = true),
        @Index(name = "idx_usuario_role", columnList = "role")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario extends BaseEntity {

    @NotBlank
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank
    @Column(name = "login", nullable = false, unique = true, length = 150)
    private String login;

    @NotBlank
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash; // BCrypt hash

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private RoleUsuario role;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private boolean ativo = true;

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public boolean isAdmin() {
        return this.role == RoleUsuario.ADMIN;
    }
}
