package com.radiocom.ordemservico.application.service;

import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemEntradaApplicationService {

    private final ItemEntradaDomainService itemDomainService;
    private final OrdemServicoMapper mapper;
    private final CatalogoModeloService catalogoModeloService;

    @Transactional
    public ItemEntradaDTO criar(ItemEntradaCreateDTO dto) {
        ItemEntrada item = itemDomainService.criar(mapper.toEntity(dto));
        log.info("Item de entrada criado: {} na OS {}", item.getId(), item.getOsId());
        return comCatalogo(mapper.toDTO(item));
    }

    @Transactional(readOnly = true)
    public ItemEntradaDTO buscarPorId(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.buscarPorId(id)));
    }

    @Transactional(readOnly = true)
    public List<ItemEntradaDTO> listarPorOS(UUID osId) {
        return comCatalogo(mapper.toItemDTOList(itemDomainService.listarPorOS(osId)));
    }

    @Transactional
    public ItemEntradaDTO avaliar(UUID id, AvaliarItemDTO dto) {
        return comCatalogo(mapper.toDTO(itemDomainService.avaliar(id, dto.getAvaliacaoTecnica(), dto.isSemDefeito())));
    }

    @Transactional
    public ItemEntradaDTO atualizarAvaliacao(UUID id, AvaliarItemDTO dto) {
        return comCatalogo(mapper.toDTO(itemDomainService.atualizarAvaliacao(id, dto.getAvaliacaoTecnica(), dto.isSemDefeito())));
    }

    @Transactional
    public ItemEntradaDTO enviarParaAutorizacao(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.enviarParaAutorizacao(id)));
    }

    @Transactional
    public ItemEntradaDTO autorizar(UUID id) {
        ItemEntrada item = itemDomainService.autorizar(id);
        log.info("Item {} autorizado — status: {}", item.getId(), item.getStatus());
        return comCatalogo(mapper.toDTO(item));
    }

    @Transactional
    public ItemEntradaDTO naoAutorizar(UUID id, MotivoDTO dto) {
        return comCatalogo(mapper.toDTO(itemDomainService.naoAutorizar(id, dto.getMotivo())));
    }

    @Transactional
    public ItemEntradaDTO iniciarManutencao(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.iniciarManutencao(id)));
    }

    @Transactional
    public ItemEntradaDTO marcarAguardandoPeca(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.marcarAguardandoPeca(id)));
    }

    @Transactional
    public ItemEntradaDTO concluirManutencao(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.concluirManutencao(id)));
    }

    @Transactional
    public ItemEntradaDTO aguardarEntrega(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.aguardarEntrega(id)));
    }

    @Transactional
    public ItemEntradaDTO entregar(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.entregar(id)));
    }

    @Transactional
    public ItemEntradaDTO adicionarItemConserto(UUID id, ItemConsertoCreateDTO dto) {
        ItemEntrada item = itemDomainService.adicionarItemConserto(id, mapper.toEntity(dto));
        return comCatalogo(mapper.toDTO(item));
    }

    @Transactional
    public ItemEntradaDTO removerItemConserto(UUID id, UUID itemConsertoId) {
        return comCatalogo(mapper.toDTO(itemDomainService.removerItemConserto(id, itemConsertoId)));
    }

    /** Resolve o valor de referência do catálogo (não vem do mapper puro, que não acessa outros módulos). */
    private ItemEntradaDTO comCatalogo(ItemEntradaDTO dto) {
        if (dto.getCatalogoModeloId() == null) return dto;
        Map<UUID, BigDecimal> valores = catalogoModeloService.buscarValoresReferenciaPorIds(List.of(dto.getCatalogoModeloId()));
        dto.setCatalogoValorReferencia(valores.get(dto.getCatalogoModeloId()));
        return dto;
    }

    private List<ItemEntradaDTO> comCatalogo(List<ItemEntradaDTO> dtos) {
        List<UUID> ids = dtos.stream()
                .map(ItemEntradaDTO::getCatalogoModeloId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return dtos;
        Map<UUID, BigDecimal> valores = catalogoModeloService.buscarValoresReferenciaPorIds(ids);
        dtos.forEach(dto -> dto.setCatalogoValorReferencia(valores.get(dto.getCatalogoModeloId())));
        return dtos;
    }
}
