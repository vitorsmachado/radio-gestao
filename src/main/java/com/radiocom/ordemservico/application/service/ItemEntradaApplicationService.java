package com.radiocom.ordemservico.application.service;

import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemEntradaApplicationService {

    private final ItemEntradaDomainService itemDomainService;
    private final OrdemServicoMapper mapper;

    @Transactional
    public ItemEntradaDTO criar(ItemEntradaCreateDTO dto) {
        ItemEntrada item = itemDomainService.criar(mapper.toEntity(dto));
        log.info("Item de entrada criado: {} na OS {}", item.getId(), item.getOsId());
        return mapper.toDTO(item);
    }

    @Transactional(readOnly = true)
    public ItemEntradaDTO buscarPorId(UUID id) {
        return mapper.toDTO(itemDomainService.buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public List<ItemEntradaDTO> listarPorOS(UUID osId) {
        return mapper.toItemDTOList(itemDomainService.listarPorOS(osId));
    }

    @Transactional
    public ItemEntradaDTO avaliar(UUID id, AvaliarItemDTO dto) {
        return mapper.toDTO(itemDomainService.avaliar(id, dto.getAvaliacaoTecnica(), dto.isSemDefeito()));
    }

    @Transactional
    public ItemEntradaDTO atualizarAvaliacao(UUID id, AvaliarItemDTO dto) {
        return mapper.toDTO(itemDomainService.atualizarAvaliacao(id, dto.getAvaliacaoTecnica(), dto.isSemDefeito()));
    }

    @Transactional
    public ItemEntradaDTO enviarParaAutorizacao(UUID id) {
        return mapper.toDTO(itemDomainService.enviarParaAutorizacao(id));
    }

    @Transactional
    public ItemEntradaDTO autorizar(UUID id) {
        ItemEntrada item = itemDomainService.autorizar(id);
        log.info("Item {} autorizado — status: {}", item.getId(), item.getStatus());
        return mapper.toDTO(item);
    }

    @Transactional
    public ItemEntradaDTO naoAutorizar(UUID id, MotivoDTO dto) {
        return mapper.toDTO(itemDomainService.naoAutorizar(id, dto.getMotivo()));
    }

    @Transactional
    public ItemEntradaDTO iniciarManutencao(UUID id) {
        return mapper.toDTO(itemDomainService.iniciarManutencao(id));
    }

    @Transactional
    public ItemEntradaDTO marcarAguardandoPeca(UUID id) {
        return mapper.toDTO(itemDomainService.marcarAguardandoPeca(id));
    }

    @Transactional
    public ItemEntradaDTO concluirManutencao(UUID id) {
        return mapper.toDTO(itemDomainService.concluirManutencao(id));
    }

    @Transactional
    public ItemEntradaDTO aguardarEntrega(UUID id) {
        return mapper.toDTO(itemDomainService.aguardarEntrega(id));
    }

    @Transactional
    public ItemEntradaDTO entregar(UUID id) {
        return mapper.toDTO(itemDomainService.entregar(id));
    }

    @Transactional
    public ItemEntradaDTO adicionarItemConserto(UUID id, ItemConsertoCreateDTO dto) {
        ItemEntrada item = itemDomainService.adicionarItemConserto(id, mapper.toEntity(dto));
        return mapper.toDTO(item);
    }

    @Transactional
    public ItemEntradaDTO removerItemConserto(UUID id, UUID itemConsertoId) {
        return mapper.toDTO(itemDomainService.removerItemConserto(id, itemConsertoId));
    }
}
