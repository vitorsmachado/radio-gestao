package com.radiocom.auth.interfaces.rest;

import com.radiocom.auth.application.dto.CriarUsuarioDTO;
import com.radiocom.auth.application.dto.LoginRequestDTO;
import com.radiocom.auth.application.dto.LoginResponseDTO;
import com.radiocom.auth.application.dto.UsuarioDTO;
import com.radiocom.auth.application.service.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e criação de usuários")
public class AuthController {

    private final AuthApplicationService service;

    @PostMapping("/login")
    @Operation(summary = "Autenticar e obter JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(service.login(dto));
    }

    @PostMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar usuário", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UsuarioDTO> criar(@Valid @RequestBody CriarUsuarioDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }
}
