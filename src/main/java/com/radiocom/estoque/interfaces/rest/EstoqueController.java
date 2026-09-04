package com.radiocom.estoque.interfaces.rest;

import com.radiocom.estoque.application.dto.*;
import com.radiocom.estoque.application.service.EstoqueApplicationService;
import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoItem;
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
@RequestMapping("/v1/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "API de gestão de equipamentos, acessórios e peças")
public class EstoqueController {

    private final EstoqueApplicationService estoqueService;

    // ========== EQUIPAMENTOS ==========

    @PostMapping("/equipamentos")
    @Operation(summary = "Criar equipamento",
            description = "NOSSO requer patrimônio. CLIENTE requer clienteId.")
    public ResponseEntity<EquipamentoDTO> criarEquipamento(@Valid @RequestBody EquipamentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueService.criarEquipamento(dto));
    }

    @GetMapping("/equipamentos/{id}")
    @Operation(summary = "Buscar equipamento por ID")
    public ResponseEntity<EquipamentoDTO> buscarEquipamentoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.buscarEquipamentoPorId(id));
    }

    @GetMapping("/equipamentos/ns/{numeroSerie}")
    @Operation(summary = "Buscar equipamento por número de série")
    public ResponseEntity<EquipamentoDTO> buscarEquipamentoPorNS(@PathVariable String numeroSerie) {
        return ResponseEntity.ok(estoqueService.buscarEquipamentoPorNS(numeroSerie));
    }

    @GetMapping("/equipamentos/patrimonio/{patrimonio}")
    @Operation(summary = "Buscar equipamento por patrimônio")
    public ResponseEntity<EquipamentoDTO> buscarEquipamentoPorPatrimonio(@PathVariable String patrimonio) {
        return ResponseEntity.ok(estoqueService.buscarEquipamentoPorPatrimonio(patrimonio));
    }

    @GetMapping("/equipamentos/estado/{estado}")
    @Operation(summary = "Listar equipamentos por estado")
    public ResponseEntity<List<EquipamentoDTO>> listarEquipamentosPorEstado(@PathVariable EstadoEquipamento estado) {
        return ResponseEntity.ok(estoqueService.listarEquipamentosPorEstado(estado));
    }

    @GetMapping("/equipamentos/proprietario/{proprietario}")
    @Operation(summary = "Listar equipamentos por proprietário (NOSSO/CLIENTE)")
    public ResponseEntity<List<EquipamentoDTO>> listarEquipamentosPorProprietario(
            @PathVariable ProprietarioEquipamento proprietario) {
        return ResponseEntity.ok(estoqueService.listarEquipamentosPorProprietario(proprietario));
    }

    @PutMapping("/equipamentos/{id}")
    @Operation(summary = "Atualizar equipamento",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados.")
    public ResponseEntity<EquipamentoDTO> atualizarEquipamento(
            @PathVariable UUID id, @Valid @RequestBody EquipamentoUpdateDTO dto) {
        return ResponseEntity.ok(estoqueService.atualizarEquipamento(id, dto));
    }

    @PatchMapping("/equipamentos/{id}/enviar-manutencao")
    @Operation(summary = "Enviar equipamento para manutenção")
    public ResponseEntity<EquipamentoDTO> enviarEquipamentoManutencao(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.enviarEquipamentoManutencao(id));
    }

    @PatchMapping("/equipamentos/{id}/concluir-manutencao")
    @Operation(summary = "Concluir manutenção do equipamento")
    public ResponseEntity<EquipamentoDTO> concluirManutencaoEquipamento(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.concluirManutencaoEquipamento(id));
    }

    @PatchMapping("/equipamentos/{id}/descartar")
    @Operation(summary = "Marcar equipamento como descartado")
    public ResponseEntity<EquipamentoDTO> marcarEquipamentoDescartado(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.marcarEquipamentoDescartado(id));
    }

    // ========== ACESSORIOS ==========

    @PostMapping("/acessorios")
    @Operation(summary = "Criar acessório")
    public ResponseEntity<AcessorioDTO> criarAcessorio(@Valid @RequestBody AcessorioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueService.criarAcessorio(dto));
    }

    @GetMapping("/acessorios/{id}")
    @Operation(summary = "Buscar acessório por ID")
    public ResponseEntity<AcessorioDTO> buscarAcessorioPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.buscarAcessorioPorId(id));
    }

    @PutMapping("/acessorios/{id}")
    @Operation(summary = "Atualizar acessório",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados.")
    public ResponseEntity<AcessorioDTO> atualizarAcessorio(
            @PathVariable UUID id, @Valid @RequestBody AcessorioUpdateDTO dto) {
        return ResponseEntity.ok(estoqueService.atualizarAcessorio(id, dto));
    }

    @GetMapping("/acessorios/{id}/saldo")
    @Operation(summary = "Consultar saldo em estoque do acessório")
    public ResponseEntity<Integer> consultarSaldoAcessorio(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.consultarSaldo(id, TipoItem.ACESSORIO));
    }

    @PostMapping("/acessorios/{id}/entrada")
    @Operation(summary = "Registrar entrada de quantidade do acessório")
    public ResponseEntity<Integer> darEntradaAcessorio(
            @PathVariable UUID id, @Valid @RequestBody MovimentacaoQuantidadeDTO dto) {
        return ResponseEntity.ok(estoqueService.darEntrada(id, TipoItem.ACESSORIO, dto.getQuantidade()));
    }

    @PostMapping("/acessorios/{id}/saida")
    @Operation(summary = "Registrar saída de quantidade do acessório")
    public ResponseEntity<Integer> darSaidaAcessorio(
            @PathVariable UUID id, @Valid @RequestBody MovimentacaoQuantidadeDTO dto) {
        return ResponseEntity.ok(estoqueService.darSaida(id, TipoItem.ACESSORIO, dto.getQuantidade()));
    }

    @PutMapping("/acessorios/{id}/ajuste")
    @Operation(summary = "Ajustar quantidade do acessório para um valor exato")
    public ResponseEntity<Integer> ajustarAcessorio(
            @PathVariable UUID id, @Valid @RequestBody AjusteQuantidadeDTO dto) {
        return ResponseEntity.ok(estoqueService.ajustarQuantidade(id, TipoItem.ACESSORIO, dto.getQuantidade()));
    }

    // ========== PECAS ==========

    @PostMapping("/pecas")
    @Operation(summary = "Criar peça")
    public ResponseEntity<PecaDTO> criarPeca(@Valid @RequestBody PecaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estoqueService.criarPeca(dto));
    }

    @GetMapping("/pecas/{id}")
    @Operation(summary = "Buscar peça por ID")
    public ResponseEntity<PecaDTO> buscarPecaPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.buscarPecaPorId(id));
    }

    @PutMapping("/pecas/{id}")
    @Operation(summary = "Atualizar peça",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados.")
    public ResponseEntity<PecaDTO> atualizarPeca(
            @PathVariable UUID id, @Valid @RequestBody PecaUpdateDTO dto) {
        return ResponseEntity.ok(estoqueService.atualizarPeca(id, dto));
    }

    @GetMapping("/pecas/{id}/saldo")
    @Operation(summary = "Consultar saldo em estoque da peça")
    public ResponseEntity<Integer> consultarSaldoPeca(@PathVariable UUID id) {
        return ResponseEntity.ok(estoqueService.consultarSaldo(id, TipoItem.PECA));
    }

    @PostMapping("/pecas/{id}/entrada")
    @Operation(summary = "Registrar entrada de quantidade da peça")
    public ResponseEntity<Integer> darEntradaPeca(
            @PathVariable UUID id, @Valid @RequestBody MovimentacaoQuantidadeDTO dto) {
        return ResponseEntity.ok(estoqueService.darEntrada(id, TipoItem.PECA, dto.getQuantidade()));
    }

    @PostMapping("/pecas/{id}/saida")
    @Operation(summary = "Registrar saída de quantidade da peça")
    public ResponseEntity<Integer> darSaidaPeca(
            @PathVariable UUID id, @Valid @RequestBody MovimentacaoQuantidadeDTO dto) {
        return ResponseEntity.ok(estoqueService.darSaida(id, TipoItem.PECA, dto.getQuantidade()));
    }

    @PutMapping("/pecas/{id}/ajuste")
    @Operation(summary = "Ajustar quantidade da peça para um valor exato")
    public ResponseEntity<Integer> ajustarPeca(
            @PathVariable UUID id, @Valid @RequestBody AjusteQuantidadeDTO dto) {
        return ResponseEntity.ok(estoqueService.ajustarQuantidade(id, TipoItem.PECA, dto.getQuantidade()));
    }
}
