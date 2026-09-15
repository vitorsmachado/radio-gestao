package com.radiocom.cliente.interfaces.rest;

import com.radiocom.cliente.application.dto.ClienteCreateDTO;
import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.dto.ClienteUpdateDTO;
import com.radiocom.cliente.application.dto.ContatoDTO;
import com.radiocom.cliente.application.dto.MotivoDTO;
import com.radiocom.cliente.application.dto.ContatoUpdateDTO;
import com.radiocom.cliente.application.dto.PostoDTO;
import com.radiocom.cliente.application.dto.PostoUpdateDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "API de gestão de clientes")
public class ClienteController {

    private final ClienteApplicationService clienteService;

    // ========== CRUD BÁSICO ==========

    @PostMapping
    @Operation(summary = "Criar novo cliente")
    public ResponseEntity<ClienteDTO> criar(@Valid @RequestBody ClienteCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/{id}/completo")
    @Operation(summary = "Buscar cliente com todos os relacionamentos")
    public ResponseEntity<ClienteDTO> buscarCompleto(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.buscarPorIdCompleto(id));
    }

    @GetMapping("/documento/{documento}")
    @Operation(summary = "Buscar cliente por documento (CPF ou CNPJ)")
    public ResponseEntity<ClienteDTO> buscarPorDocumento(@PathVariable String documento) {
        return ResponseEntity.ok(clienteService.buscarPorDocumento(documento));
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes",
            description = "Ordenado por número de identificação (mais recente primeiro) por padrão.")
    public ResponseEntity<Page<ClienteDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "numeroIdentificacao", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarTodos(pageable));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar cliente",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados. "
                    + "tipo e documento são imutáveis. "
                    + "Para status use PATCH /{id}/ativar, /inativar ou /bloquear."
    )
    public ResponseEntity<ClienteDTO> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.atualizar(id, dto));
    }

    // ========== CONSULTAS ESPECÍFICAS ==========

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar clientes por status")
    public ResponseEntity<List<ClienteDTO>> listarPorStatus(@PathVariable StatusCliente status) {
        return ResponseEntity.ok(clienteService.listarPorStatus(status));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Listar clientes por tipo (FISICA/JURIDICA)")
    public ResponseEntity<Page<ClienteDTO>> listarPorTipo(
            @PathVariable TipoPessoa tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clienteService.buscarPorTipo(tipo, pageable));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar clientes por nome")
    public ResponseEntity<Page<ClienteDTO>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(clienteService.buscarPorNome(nome, pageable));
    }

    // ========== AÇÕES DE STATUS ==========

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar cliente", description = "Motivo é opcional.")
    public ResponseEntity<ClienteDTO> ativar(
            @PathVariable UUID id, @RequestBody(required = false) @Valid MotivoDTO dto) {
        return ResponseEntity.ok(clienteService.ativar(id, dto));
    }

    @PatchMapping("/{id}/inativar")
    @Operation(summary = "Inativar cliente", description = "Motivo é opcional.")
    public ResponseEntity<ClienteDTO> inativar(
            @PathVariable UUID id, @RequestBody(required = false) @Valid MotivoDTO dto) {
        return ResponseEntity.ok(clienteService.inativar(id, dto));
    }

    @PatchMapping("/{id}/bloquear")
    @Operation(summary = "Bloquear cliente", description = "Motivo é opcional.")
    public ResponseEntity<ClienteDTO> bloquear(
            @PathVariable UUID id, @RequestBody(required = false) @Valid MotivoDTO dto) {
        return ResponseEntity.ok(clienteService.bloquear(id, dto));
    }

    // ========== GESTÃO DE POSTOS ==========

    @GetMapping("/{id}/postos")
    @Operation(summary = "Listar postos do cliente")
    public ResponseEntity<List<PostoDTO>> listarPostos(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteService.listarPostos(id));
    }

    @PostMapping("/{id}/postos")
    @Operation(summary = "Adicionar posto ao cliente")
    public ResponseEntity<PostoDTO> adicionarPosto(
            @PathVariable UUID id,
            @Valid @RequestBody PostoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(clienteService.adicionarPosto(id, dto));
    }

    @DeleteMapping("/{id}/postos/{postoId}")
    @Operation(summary = "Remover posto do cliente")
    public ResponseEntity<ClienteDTO> removerPosto(
            @PathVariable UUID id,
            @PathVariable UUID postoId) {
        return ResponseEntity.ok(clienteService.removerPosto(id, postoId));
    }

    @PutMapping("/{id}/postos/{postoId}")
    @Operation(
            summary = "Atualizar posto",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados."
    )
    public ResponseEntity<ClienteDTO> atualizarPosto(
            @PathVariable UUID id,
            @PathVariable UUID postoId,
            @Valid @RequestBody PostoUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.atualizarPosto(id, postoId, dto));
    }

    // ========== GESTÃO DE CONTATOS ==========

    @PostMapping("/{id}/contatos")
    @Operation(summary = "Adicionar contato ao cliente")
    public ResponseEntity<ClienteDTO> adicionarContato(
            @PathVariable UUID id,
            @Valid @RequestBody ContatoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).
                body(clienteService.adicionarContato(id, dto));
    }

    @DeleteMapping("/{id}/contatos/{contatoId}")
    @Operation(summary = "Remover contato do cliente")
    public ResponseEntity<ClienteDTO> removerContato(
            @PathVariable UUID id,
            @PathVariable UUID contatoId) {
        return ResponseEntity.ok(clienteService.removerContato(id, contatoId));
    }

    @PutMapping("/{id}/contatos/{contatoId}")
    @Operation(
            summary = "Atualizar contato",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados. "
                    + "Para alterar o contato principal use PATCH /{id}/contatos/{contatoId}/principal."
    )
    public ResponseEntity<ClienteDTO> atualizarContato(
            @PathVariable UUID id,
            @PathVariable UUID contatoId,
            @Valid @RequestBody ContatoUpdateDTO dto) {
        return ResponseEntity.ok(clienteService.atualizarContato(id, contatoId, dto));
    }

    @PatchMapping("/{id}/contatos/{contatoId}/principal")
    @Operation(summary = "Definir contato como principal")
    public ResponseEntity<ClienteDTO> atualizarContatoPrincipal(
            @PathVariable UUID id,
            @PathVariable UUID contatoId) {
        return ResponseEntity.ok(clienteService.atualizarContatoPrincipal(id, contatoId));
    }
}
