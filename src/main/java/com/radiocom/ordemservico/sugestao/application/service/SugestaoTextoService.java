package com.radiocom.ordemservico.sugestao.application.service;

import com.radiocom.ordemservico.sugestao.application.dto.SugestaoTextoDTO;
import com.radiocom.ordemservico.sugestao.domain.model.SugestaoTexto;
import com.radiocom.ordemservico.sugestao.domain.model.enums.CampoSugestao;
import com.radiocom.ordemservico.sugestao.domain.repository.SugestaoTextoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Valores já digitados antes nos campos de laudo técnico, reaproveitáveis
 * como sugestão de autocomplete. Registro de uso é automático (chamado ao
 * salvar uma avaliação técnica); exclusão é manual, pelo técnico.
 */
@Service
@RequiredArgsConstructor
public class SugestaoTextoService {

    private static final int LIMITE_SUGESTOES = 8;

    private final SugestaoTextoRepository repository;

    @Transactional(readOnly = true)
    public List<SugestaoTextoDTO> buscar(CampoSugestao campo, String busca) {
        String buscaTratada = busca != null && !busca.isBlank() ? busca.trim() : null;
        return repository.buscar(campo, buscaTratada, PageRequest.of(0, LIMITE_SUGESTOES)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /** Upsert — incrementa a contagem de uso se o texto já existe pra esse campo. */
    @Transactional
    public void registrarUso(CampoSugestao campo, String valor) {
        if (valor == null || valor.isBlank()) return;
        String valorTratado = valor.trim();
        Optional<SugestaoTexto> existente = repository.findByCampoAndValorIgnoreCase(campo, valorTratado);
        if (existente.isPresent()) {
            existente.get().registrarUso();
        } else {
            repository.save(SugestaoTexto.builder().campo(campo).valor(valorTratado).build());
        }
    }

    @Transactional
    public void excluir(UUID id) {
        if (!repository.existsById(id)) {
            throw new DomainException("Sugestão não encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private SugestaoTextoDTO toDTO(SugestaoTexto s) {
        return SugestaoTextoDTO.builder()
                .id(s.getId())
                .valor(s.getValor())
                .contagemUso(s.getContagemUso())
                .build();
    }
}
