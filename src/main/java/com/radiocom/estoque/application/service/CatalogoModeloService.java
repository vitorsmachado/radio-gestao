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

import java.util.UUID;

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
        mapper.updateEntityFromDTO(dto, entidade);
        return mapper.toDTO(repository.save(entidade));
    }

    @Transactional(readOnly = true)
    public Page<CatalogoModeloDTO> listar(String busca, TipoItem tipoItem, StatusItem status, Pageable pageable) {
        return repository.buscar(busca, tipoItem, status, pageable).map(mapper::toDTO);
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
