package com.radiocom.notificacao.interfaces.rest;

import com.radiocom.notificacao.application.dto.NotificacaoDTO;
import com.radiocom.notificacao.application.service.NotificacaoApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/notificacoes")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Avisos pro admin agir — hoje só conflito de garantia")
public class NotificacaoController {

    private final NotificacaoApplicationService service;

    @GetMapping
    @Operation(summary = "Listar notificações",
            description = "lida=false traz só as não lidas; sem o parâmetro traz todas, mais recentes primeiro.")
    public ResponseEntity<Page<NotificacaoDTO>> listar(
            @RequestParam(required = false) Boolean lida,
            @PageableDefault(size = 20, sort = "dataCriacao", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(lida, pageable));
    }

    @GetMapping("/contagem-nao-lidas")
    @Operation(summary = "Contar notificações não lidas")
    public ResponseEntity<Long> contarNaoLidas() {
        return ResponseEntity.ok(service.contarNaoLidas());
    }

    @PatchMapping("/{id}/marcar-lida")
    @Operation(summary = "Marcar notificação como lida")
    public ResponseEntity<NotificacaoDTO> marcarComoLida(@PathVariable UUID id) {
        return ResponseEntity.ok(service.marcarComoLida(id));
    }
}
