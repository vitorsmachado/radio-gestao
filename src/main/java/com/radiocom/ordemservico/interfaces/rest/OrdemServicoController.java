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
