package com.radiocom.orcamento.interfaces.rest;

import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.orcamento.application.dto.AdicionarItemOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoCreateDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.service.OrcamentoApplicationService;
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
@RequestMapping("/v1/orcamentos")
@RequiredArgsConstructor
@Tag(name = "Orçamentos", description = "Propostas de conserto agrupando itens de entrada de uma OS")
public class OrcamentoController {

    private final OrcamentoApplicationService service;

    @PostMapping
    @Operation(summary = "Criar novo orçamento (RASCUNHO)")
    public ResponseEntity<OrcamentoDTO> criar(@Valid @RequestBody OrcamentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID")
    public ResponseEntity<OrcamentoDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/numero/{numero}")
    @Operation(summary = "Buscar orçamento por número")
    public ResponseEntity<OrcamentoDTO> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(service.buscarPorNumero(numero));
    }

    @GetMapping
    @Operation(summary = "Listar orçamentos de uma OS")
    public ResponseEntity<List<OrcamentoDTO>> listarPorOS(@RequestParam UUID osId) {
        return ResponseEntity.ok(service.listarPorOS(osId));
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adicionar um item de entrada ao orçamento",
            description = "O item precisa pertencer à mesma OS do orçamento.")
    public ResponseEntity<OrcamentoDTO> adicionarItem(
            @PathVariable UUID id, @Valid @RequestBody AdicionarItemOrcamentoDTO dto) {
        return ResponseEntity.ok(service.adicionarItem(id, dto));
    }

    @DeleteMapping("/{id}/itens/{itemEntradaId}")
    @Operation(summary = "Remover um item do orçamento")
    public ResponseEntity<OrcamentoDTO> removerItem(
            @PathVariable UUID id, @PathVariable UUID itemEntradaId) {
        return ResponseEntity.ok(service.removerItem(id, itemEntradaId));
    }

    @PatchMapping("/{id}/enviar")
    @Operation(summary = "Enviar orçamento ao cliente",
            description = "Marca o orçamento como apresentado e os itens ainda AVALIADO passam "
                    + "para PENDENTE_AUTORIZACAO. A aprovação/rejeição em si continua sendo por item.")
    public ResponseEntity<OrcamentoDTO> enviar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.enviar(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar o orçamento")
    public ResponseEntity<OrcamentoDTO> cancelar(
            @PathVariable UUID id, @Valid @RequestBody MotivoDTO dto) {
        return ResponseEntity.ok(service.cancelar(id, dto));
    }
}
