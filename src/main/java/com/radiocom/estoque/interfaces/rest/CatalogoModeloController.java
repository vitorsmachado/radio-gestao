package com.radiocom.estoque.interfaces.rest;

import com.radiocom.estoque.application.dto.CatalogoModeloCreateDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloUpdateDTO;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.estoque.domain.model.enums.TipoItem;
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
@RequestMapping("/v1/catalogo-modelos")
@RequiredArgsConstructor
@Tag(name = "Catálogo de Modelos", description = "API de catálogo de marcas/modelos de equipamentos, acessórios e peças")
public class CatalogoModeloController {

    private final CatalogoModeloService service;

    @PostMapping
    @Operation(summary = "Criar entrada de catálogo")
    public ResponseEntity<CatalogoModeloDTO> criar(@Valid @RequestBody CatalogoModeloCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar entrada de catálogo por ID")
    public ResponseEntity<CatalogoModeloDTO> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar entrada de catálogo",
            description = "Atualização parcial: apenas campos enviados (não-nulos) são aplicados.")
    public ResponseEntity<CatalogoModeloDTO> atualizar(
            @PathVariable UUID id, @Valid @RequestBody CatalogoModeloUpdateDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @GetMapping("/tipo/{tipoItem}")
    @Operation(summary = "Listar entradas de catálogo por tipo (EQUIPAMENTO/ACESSORIO/PECA/SERVICO)")
    public ResponseEntity<List<CatalogoModeloDTO>> listarPorTipo(@PathVariable TipoItem tipoItem) {
        return ResponseEntity.ok(service.listarPorTipo(tipoItem));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar por marca ou modelo")
    public ResponseEntity<Page<CatalogoModeloDTO>> buscarPorMarcaOuModelo(
            @RequestParam String termo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.buscarPorMarcaOuModelo(termo, pageable));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover entrada de catálogo")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
