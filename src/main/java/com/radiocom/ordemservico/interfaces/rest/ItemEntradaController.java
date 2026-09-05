package com.radiocom.ordemservico.interfaces.rest;

import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.service.ItemEntradaApplicationService;
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
@RequestMapping("/v1/itens-entrada")
@RequiredArgsConstructor
@Tag(name = "Itens de Entrada", description = "Equipamentos/acessórios trazidos pelo cliente numa OS")
public class ItemEntradaController {

    private final ItemEntradaApplicationService itemService;
    private final OrdemServicoApplicationService osService;

    @PostMapping
    @Operation(summary = "Registrar item de entrada numa OS")
    public ResponseEntity<ItemEntradaDTO> criar(@Valid @RequestBody ItemEntradaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar item de entrada por ID")
    public ResponseEntity<ItemEntradaDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar itens de entrada de uma OS")
    public ResponseEntity<List<ItemEntradaDTO>> listarPorOS(@RequestParam UUID osId) {
        return ResponseEntity.ok(itemService.listarPorOS(osId));
    }

    @PatchMapping("/{id}/avaliar")
    @Operation(summary = "Registrar a primeira avaliação técnica do item")
    public ResponseEntity<ItemEntradaDTO> avaliar(
            @PathVariable UUID id, @Valid @RequestBody AvaliarItemDTO dto) {
        return ResponseEntity.ok(itemService.avaliar(id, dto));
    }

    @PutMapping("/{id}/avaliacao")
    @Operation(summary = "Atualizar o laudo",
            description = "Permitido em qualquer momento antes da entrega — inclusive depois de "
                    + "autorizado, ou ao encontrar um problema novo já na hora da entrega.")
    public ResponseEntity<ItemEntradaDTO> atualizarAvaliacao(
            @PathVariable UUID id, @Valid @RequestBody AvaliarItemDTO dto) {
        return ResponseEntity.ok(itemService.atualizarAvaliacao(id, dto));
    }

    @PatchMapping("/{id}/enviar-autorizacao")
    @Operation(summary = "Marcar orçamento como apresentado ao cliente")
    public ResponseEntity<ItemEntradaDTO> enviarParaAutorizacao(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.enviarParaAutorizacao(id));
    }

    @PatchMapping("/{id}/autorizar")
    @Operation(summary = "Autorizar o conserto",
            description = "Decide automaticamente entre fila de manutenção ou aguardando peça, "
                    + "conforme a disponibilidade em estoque das peças do conserto.")
    public ResponseEntity<ItemEntradaDTO> autorizar(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.autorizar(id));
    }

    @PatchMapping("/{id}/nao-autorizar")
    @Operation(summary = "Registrar que o cliente não autorizou o conserto")
    public ResponseEntity<ItemEntradaDTO> naoAutorizar(
            @PathVariable UUID id, @Valid @RequestBody MotivoDTO dto) {
        return ResponseEntity.ok(itemService.naoAutorizar(id, dto));
    }

    @PatchMapping("/{id}/iniciar-manutencao")
    @Operation(summary = "Iniciar o reparo físico do item")
    public ResponseEntity<ItemEntradaDTO> iniciarManutencao(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.iniciarManutencao(id));
    }

    @PatchMapping("/{id}/marcar-aguardando-peca")
    @Operation(summary = "Marcar que falta peça durante o reparo",
            description = "Uso manual pelo técnico ao descobrir, no meio do conserto, que falta "
                    + "uma peça que não estava prevista na autorização.")
    public ResponseEntity<ItemEntradaDTO> marcarAguardandoPeca(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.marcarAguardandoPeca(id));
    }

    @PatchMapping("/{id}/concluir-manutencao")
    @Operation(summary = "Concluir o reparo físico do item")
    public ResponseEntity<ItemEntradaDTO> concluirManutencao(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.concluirManutencao(id));
    }

    @PatchMapping("/{id}/aguardar-entrega")
    @Operation(summary = "Marcar item como pronto, aguardando o cliente vir buscar")
    public ResponseEntity<ItemEntradaDTO> aguardarEntrega(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.aguardarEntrega(id));
    }

    @PatchMapping("/{id}/entregar")
    @Operation(summary = "Confirmar entrega do item ao cliente")
    public ResponseEntity<ItemEntradaDTO> entregar(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.entregar(id));
    }

    @PatchMapping("/{id}/mover")
    @Operation(summary = "Mover o item para outra OS")
    public ResponseEntity<Void> mover(@PathVariable UUID id, @Valid @RequestBody MoverItemDTO dto) {
        osService.moverItem(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/itens-conserto")
    @Operation(summary = "Adicionar peça/mão de obra/deslocamento ao conserto do item")
    public ResponseEntity<ItemEntradaDTO> adicionarItemConserto(
            @PathVariable UUID id, @Valid @RequestBody ItemConsertoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.adicionarItemConserto(id, dto));
    }

    @DeleteMapping("/{id}/itens-conserto/{itemConsertoId}")
    @Operation(summary = "Remover um item de conserto")
    public ResponseEntity<ItemEntradaDTO> removerItemConserto(
            @PathVariable UUID id, @PathVariable UUID itemConsertoId) {
        return ResponseEntity.ok(itemService.removerItemConserto(id, itemConsertoId));
    }
}
