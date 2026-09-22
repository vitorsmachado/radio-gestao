package com.radiocom.estoque.application.service;

import com.radiocom.estoque.application.dto.CatalogoModeloCreateDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloDTO;
import com.radiocom.estoque.application.dto.CatalogoModeloUpdateDTO;
import com.radiocom.estoque.application.mapper.EstoqueMapper;
import com.radiocom.estoque.domain.model.CatalogoModelo;
import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.repository.CatalogoModeloRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogoModeloService {

    private final CatalogoModeloRepository repository;
    private final EstoqueMapper mapper;

    @Transactional
    public CatalogoModeloDTO criar(CatalogoModeloCreateDTO dto) {
        repository.findByTipoItemAndMarcaIgnoreCaseAndModeloIgnoreCase(
                dto.getTipoItem(), dto.getMarca(), dto.getModelo())
                .ifPresent(c -> {
                    throw new DomainException(
                            "Já existe no catálogo: " + dto.getTipoItem() + " "
                                    + dto.getMarca() + " " + dto.getModelo());
                });

        CatalogoModelo salvo = repository.save(mapper.toEntity(dto));
        log.info("Criando catálogo: {} {} {}", dto.getTipoItem(), dto.getMarca(), dto.getModelo());
        return mapper.toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public CatalogoModeloDTO buscarPorId(UUID id) {
        return mapper.toDTO(buscarEntidade(id));
    }

    @Transactional
    public CatalogoModeloDTO atualizar(UUID id, CatalogoModeloUpdateDTO dto) {
        CatalogoModelo entidade = buscarEntidade(id);
        if (dto.getReferencia() != null && !dto.getReferencia().isBlank()
                && !dto.getReferencia().trim().equalsIgnoreCase(entidade.getReferencia())
                && repository.existsByReferenciaIgnoreCase(dto.getReferencia().trim())) {
            throw new DomainException("Referência já cadastrada: " + dto.getReferencia());
        }
        mapper.updateEntityFromDTO(dto, entidade);
        return mapper.toDTO(repository.save(entidade));
    }

    @Transactional(readOnly = true)
    public Page<CatalogoModeloDTO> listar(String busca, TipoItem tipoItem, StatusItem status, Pageable pageable) {
        return repository.buscar(busca, tipoItem, status, pageable).map(mapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<String> listarMarcas() {
        return repository.listarMarcas();
    }

    /** Usado por outros módulos (ex.: ItemEntrada) pra resolver o valor de referência sem expor a entidade. */
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> buscarValoresReferenciaPorIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repository.findAllById(ids).stream()
                .filter(c -> c.getValorReferencia() != null)
                .collect(Collectors.toMap(CatalogoModelo::getId, CatalogoModelo::getValorReferencia));
    }

    @Transactional
    public void deletar(UUID id) {
        buscarEntidade(id);
        repository.deleteById(id);
        log.info("Entrada de catálogo removida: {}", id);
    }

    private CatalogoModelo buscarEntidade(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DomainException("Entrada de catálogo não encontrada: " + id));
    }
}
