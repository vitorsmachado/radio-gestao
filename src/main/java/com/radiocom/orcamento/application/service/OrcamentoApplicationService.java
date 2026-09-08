package com.radiocom.orcamento.application.service;

import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.orcamento.application.dto.AdicionarItemOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoCreateDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.mapper.OrcamentoMapper;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrcamentoApplicationService {

    private final OrcamentoDomainService orcamentoDomainService;
    private final OrcamentoMapper mapper;
    private final OrdemServicoMapper itemMapper;

    @Transactional
    public OrcamentoDTO criar(OrcamentoCreateDTO dto) {
        Orcamento orcamento = orcamentoDomainService.criar(
                dto.getOsId(), dto.getClienteId(), dto.getValidade(),
                dto.getCondicoesPagamento(), dto.getDesconto());
        log.info("Orçamento criado: {} ({})", orcamento.getId(), orcamento.getNumero());
        return toDTOComItens(orcamento);
    }

    @Transactional(readOnly = true)
    public OrcamentoDTO buscarPorId(UUID id) {
        return toDTOComItens(orcamentoDomainService.buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public OrcamentoDTO buscarPorNumero(String numero) {
        return toDTOComItens(orcamentoDomainService.buscarPorNumero(numero));
    }

    @Transactional(readOnly = true)
    public List<OrcamentoDTO> listarPorOS(UUID osId) {
        return orcamentoDomainService.listarPorOS(osId).stream()
                .map(this::toDTOComItens)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrcamentoDTO adicionarItem(UUID orcamentoId, AdicionarItemOrcamentoDTO dto) {
        orcamentoDomainService.adicionarItem(orcamentoId, dto.getItemEntradaId());
        return toDTOComItens(orcamentoDomainService.buscarPorId(orcamentoId));
    }

    @Transactional
    public OrcamentoDTO removerItem(UUID orcamentoId, UUID itemEntradaId) {
        orcamentoDomainService.removerItem(orcamentoId, itemEntradaId);
        return toDTOComItens(orcamentoDomainService.buscarPorId(orcamentoId));
    }

    @Transactional
    public OrcamentoDTO enviar(UUID id) {
        Orcamento orcamento = orcamentoDomainService.enviar(id);
        log.info("Orçamento {} enviado para autorização", orcamento.getNumero());
        return toDTOComItens(orcamento);
    }

    @Transactional
    public OrcamentoDTO cancelar(UUID id, MotivoDTO dto) {
        return toDTOComItens(orcamentoDomainService.cancelar(id, dto.getMotivo()));
    }

    private OrcamentoDTO toDTOComItens(Orcamento orcamento) {
        OrcamentoDTO dto = mapper.toDTO(orcamento);
        dto.setItens(itemMapper.toItemDTOList(orcamentoDomainService.listarItens(orcamento.getId())));
        dto.setValorTotal(orcamentoDomainService.calcularTotal(orcamento.getId()));
        dto.setStatusAprovacao(orcamentoDomainService.calcularStatusAprovacao(orcamento.getId()));
        return dto;
    }
}
