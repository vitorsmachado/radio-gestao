package com.radiocom.ordemservico.interfaces.rest;

import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.service.OrdemServicoApplicationService;
import com.radiocom.ordemservico.application.service.OrdemServicoPdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de Serviço", description = "API de gestão de ordens de serviço")
public class OrdemServicoController {

    private final OrdemServicoApplicationService service;
    private final OrdemServicoPdfService pdfService;

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

    @PutMapping("/{id}")
    @Operation(summary = "Editar dados da OS",
            description = "Permitido em qualquer status, menos CONCLUÍDA. Inclui trocar o cliente vinculado.")
    public ResponseEntity<OrdemServicoDTO> atualizar(
            @PathVariable UUID id, @Valid @RequestBody AtualizarOrdemServicoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
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

    @GetMapping("/itens/{itemEstoqueId}/historico")
    @Operation(summary = "Histórico de OS de um equipamento/acessório",
            description = "Usado pela aba Garantia do detalhe do cliente.")
    public ResponseEntity<List<HistoricoOSItemDTO>> listarHistoricoPorItemEstoque(@PathVariable UUID itemEstoqueId) {
        return ResponseEntity.ok(service.listarHistoricoPorItemEstoque(itemEstoqueId));
    }

    @GetMapping("/busca")
    @Operation(summary = "Listagem geral de OS com busca e filtros",
            description = "Ordenado por número (mais recente primeiro) por padrão — sobrescrevível via "
                    + "?sort=dataAtualizacao,desc ou ?sort=dataAbertura,desc. 'busca' casa com número da OS, "
                    + "N/S ou código do cliente dos itens, e nome/documento do cliente. Período opcional "
                    + "filtra pela data de abertura.")
    public ResponseEntity<Page<OrdemServicoResumoDTO>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @PageableDefault(size = 20, sort = "numero", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(service.listar(busca, dataInicial, dataFinal, pageable));
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

    @PatchMapping("/{id}/reordenar-fila")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reordenar a OS na fila de manutenção",
            description = "Somente ADMIN. Sobe/desce uma posição ou vai pra um índice específico (arrastar) — "
                    + "sempre dentro do bloco em que a OS está agora. Devolve a fila inteira já reordenada.")
    public ResponseEntity<List<FilaManutencaoOSDTO>> reordenarFila(
            @PathVariable UUID id, @Valid @RequestBody ReordenarFilaDTO dto) {
        return ResponseEntity.ok(service.reordenarFila(id, dto));
    }

    @GetMapping("/fila-manutencao")
    @Operation(summary = "Fila de manutenção do técnico",
            description = "Uma linha por OS com itens em status em avaliação, aguardando avaliação, "
                    + "aguardando manutenção ou aguardando peça — já na ordem final de exibição.")
    public ResponseEntity<List<FilaManutencaoOSDTO>> listarFilaManutencao() {
        return ResponseEntity.ok(service.listarFilaManutencao());
    }

    @PostMapping("/{id}/separar")
    @Operation(summary = "Separar itens da OS em uma ou mais OS novas",
            description = "Cada grupo de itens vira uma OS nova, tudo numa única transação — "
                    + "útil quando o cliente aprova só parte dos itens, quando alguns itens já "
                    + "podem ser entregues enquanto outros aguardam peça, ou quando dá pra formar "
                    + "mais de um kit de uma vez.")
    public ResponseEntity<List<OrdemServicoDTO>> separar(
            @PathVariable UUID id, @Valid @RequestBody SepararOSDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.separar(id, dto));
    }

    @PostMapping("/unir")
    @Operation(summary = "Unir OS numa OS nova",
            description = "Cria uma OS nova, move pra ela todos os itens das OS selecionadas e cancela as "
                    + "origens que ficarem vazias. Todas precisam ser do mesmo cliente.")
    public ResponseEntity<OrdemServicoDTO> unir(@Valid @RequestBody UnirOSDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.unir(dto));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Gerar PDF da OS")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable UUID id) {
        byte[] pdf = pdfService.gerarPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("os-" + id + ".pdf").build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
