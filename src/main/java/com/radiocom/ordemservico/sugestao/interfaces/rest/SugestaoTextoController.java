package com.radiocom.ordemservico.sugestao.interfaces.rest;

import com.radiocom.ordemservico.sugestao.application.dto.SugestaoTextoDTO;
import com.radiocom.ordemservico.sugestao.application.service.SugestaoTextoService;
import com.radiocom.ordemservico.sugestao.domain.model.enums.CampoSugestao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/sugestoes")
@RequiredArgsConstructor
@Tag(name = "Sugestões de texto", description = "Autocomplete reaproveitável dos campos de laudo técnico")
public class SugestaoTextoController {

    private final SugestaoTextoService service;

    @GetMapping
    @Operation(summary = "Buscar sugestões de um campo",
            description = "Ordenado por mais usada primeiro. 'busca' filtra por substring (opcional).")
    public ResponseEntity<List<SugestaoTextoDTO>> buscar(
            @RequestParam CampoSugestao campo,
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(service.buscar(campo, busca));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma sugestão")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
