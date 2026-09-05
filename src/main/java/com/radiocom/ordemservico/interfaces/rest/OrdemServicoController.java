package com.radiocom.ordemservico.interfaces.rest;

import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "API de gestão de ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoApplicationService service;

    @PostMapping
    @Operation(summary = "Abrir nova OS")
    public ResponseEntity<OrdemServicoDTO> criar(@Valid @RequestBody OrdemServicoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar OS por ID")
    public ResponseEntity<OrdemServicoDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Buscar OS por número")
    public ResponseEntity<OrdemServicoDTO> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(service.buscarPorNumero(numero));
    }

    @GetMapping
    @Operation(summary = "Listar OS por cliente")
    public ResponseEntity<List<OrdemServicoDTO>> listarPorCliente(@RequestParam UUID clienteId) {
        return ResponseEntity.ok(service.listarPorCliente(clienteId));
    }

    @PatchMapping("/{id}/iniciar-andamento")
    @Operation(summary = "Iniciar andamento da OS")
    public ResponseEntity<OrdemServicoDTO> iniciarAndamento(@PathVariable UUID id) {
        return ResponseEntity.ok(service.iniciarAndamento(id));
    }

    @PatchMapping("/{id}/confirmar-entrega")
    @Operation(summary = "Confirmar entrega ao cliente e concluir a OS")
    public ResponseEntity<OrdemServicoDTO> confirmarEntrega(
            @PathVariable UUID id, @Valid @RequestBody ConfirmarEntregaDTO dto) {
        return ResponseEntity.ok(service.confirmarEntrega(id, dto));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar a OS")
    public ResponseEntity<OrdemServicoDTO> cancelar(
            @PathVariable UUID id, @Valid @RequestBody MotivoDTO dto) {
        return ResponseEntity.ok(service.cancelar(id, dto));
    }

    @PostMapping("/{id}/dividir")
    @Operation(summary = "Dividir a OS",
            description = "Cria uma OS nova e move os itens escolhidos pra ela — "
                    + "útil quando o cliente aprova só parte dos itens, ou quando alguns "
                    + "itens já podem ser entregues enquanto outros aguardam peça.")
    public ResponseEntity<OrdemServicoDTO> dividir(
            @PathVariable UUID id, @Valid @RequestBody DividirOSDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.dividir(id, dto));
    }

    @PostMapping("/{id}/unir")
    @Operation(summary = "Unir outras OS a esta",
            description = "Move todos os itens das OS de origem para esta OS e cancela as origens.")
    public ResponseEntity<OrdemServicoDTO> unir(
            @PathVariable UUID id, @Valid @RequestBody UnirOSDTO dto) {
        return ResponseEntity.ok(service.unir(id, dto));
    }
}
