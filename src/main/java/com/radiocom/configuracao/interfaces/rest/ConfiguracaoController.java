package com.radiocom.configuracao.interfaces.rest;

import com.radiocom.configuracao.application.dto.AtualizarConfiguracaoDTO;
import com.radiocom.configuracao.application.dto.ConfiguracaoDTO;
import com.radiocom.configuracao.application.service.ConfiguracaoApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Valores gerais configuráveis do sistema")
public class ConfiguracaoController {

    private final ConfiguracaoApplicationService service;

    @GetMapping
    @Operation(summary = "Buscar as configurações do sistema")
    public ResponseEntity<ConfiguracaoDTO> buscar() {
        return ResponseEntity.ok(service.buscar());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar as configurações do sistema")
    public ResponseEntity<ConfiguracaoDTO> atualizar(@Valid @RequestBody AtualizarConfiguracaoDTO dto) {
        return ResponseEntity.ok(service.atualizar(dto));
    }
}
