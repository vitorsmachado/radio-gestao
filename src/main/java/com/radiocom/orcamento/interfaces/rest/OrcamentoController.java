package com.radiocom.orcamento.interfaces.rest;

import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.orcamento.application.dto.AdicionarItemOrcamentoDTO;
import com.radiocom.orcamento.application.dto.AtualizarOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoCreateDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoResumoDTO;
import com.radiocom.orcamento.application.service.OrcamentoApplicationService;
import com.radiocom.orcamento.application.service.OrcamentoPdfService;
import com.radiocom.orcamento.application.service.OrcamentoPdfService.Agrupamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    private final OrcamentoPdfService pdfService;

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

    @GetMapping("/busca")
    @Operation(summary = "Listagem geral de orçamentos com busca e filtros",
            description = "'busca' casa com número da OS de origem, nome/documento do cliente ou N/S de um item agrupado.")
    public ResponseEntity<Page<OrcamentoResumoDTO>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) StatusOrcamento status,
            @PageableDefault(size = 20, sort = "numero", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(busca, status, pageable));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar validade, condições de pagamento e/ou desconto",
            description = "Só permitido enquanto o orçamento estiver em RASCUNHO.")
    public ResponseEntity<OrcamentoDTO> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarOrcamentoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
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

    @PatchMapping("/{id}/reabrir")
    @Operation(summary = "Reabrir orçamento enviado para edição",
            description = "Volta o orçamento ENVIADO para RASCUNHO, liberando peças, valores, desconto e "
                    + "condições pra edição. Precisa ser enviado de novo depois de ajustado.")
    public ResponseEntity<OrcamentoDTO> reabrir(@PathVariable UUID id) {
        return ResponseEntity.ok(service.reabrir(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar o orçamento")
    public ResponseEntity<OrcamentoDTO> cancelar(
            @PathVariable UUID id, @Valid @RequestBody MotivoDTO dto) {
        return ResponseEntity.ok(service.cancelar(id, dto));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Gerar PDF do orçamento",
            description = "agrupamento=EQUIPAMENTO (padrão) mostra os itens de conserto sob cada equipamento; "
                    + "agrupamento=ITENS mostra uma lista consolidada por descrição, somando quantidades.")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "EQUIPAMENTO") Agrupamento agrupamento) {
        byte[] pdf = pdfService.gerarPdf(id, agrupamento);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("orcamento-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
