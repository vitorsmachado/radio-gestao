package com.radiocom.ordemservico.application.service;

import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.ordemservico.application.dto.*;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.event.GarantiaConflitoEvent;
import com.radiocom.ordemservico.domain.event.ItemAvaliadoEvent;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.ordemservico.garantia.application.dto.GarantiaPecaDTO;
import com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
import com.radiocom.ordemservico.sugestao.application.service.SugestaoTextoService;
import com.radiocom.ordemservico.sugestao.domain.model.enums.CampoSugestao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemEntradaApplicationService {

    private final ItemEntradaDomainService itemDomainService;
    private final OrdemServicoDomainService ordemServicoDomainService;
    private final OrdemServicoMapper mapper;
    private final CatalogoModeloService catalogoModeloService;
    private final SugestaoTextoService sugestaoTextoService;
    private final GarantiaPecaDomainService garantiaPecaDomainService;
    private final ApplicationEventPublisher eventPublisher;

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
    public ItemEntradaDTO iniciarAvaliacao(UUID id) {
        ItemEntrada item = itemDomainService.iniciarAvaliacao(id);
        ordemServicoDomainService.garantirAndamento(item.getOsId());
        return comCatalogo(mapper.toDTO(item));
    }

    /**
     * Salva o laudo estruturado e registra cada campo de texto preenchido
     * como sugestão futura (upsert por conteúdo — mais usadas sobem no
     * autocomplete).
     */
    @Transactional
    public ItemEntradaDTO salvarAvaliacaoTecnica(UUID id, SalvarAvaliacaoTecnicaDTO dto) {
        ItemEntrada item = itemDomainService.salvarAvaliacaoTecnica(id, dto.getResultado(), dto.getDetalheAjuste(),
                dto.getDefeitoEncontrado(), dto.getCausaDefeito(), dto.getSolucaoRecomendada(), dto.getObservacoesTecnicas(),
                dto.getGarantiaPecaIds());
        sugestaoTextoService.registrarUso(CampoSugestao.DEFEITO_ENCONTRADO, dto.getDefeitoEncontrado());
        sugestaoTextoService.registrarUso(CampoSugestao.CAUSA_DEFEITO, dto.getCausaDefeito());
        sugestaoTextoService.registrarUso(CampoSugestao.SOLUCAO_RECOMENDADA, dto.getSolucaoRecomendada());
        sugestaoTextoService.registrarUso(CampoSugestao.OBSERVACOES_TECNICAS, dto.getObservacoesTecnicas());
        if (dto.getResultado() != ResultadoAvaliacao.SEM_DEFEITO) {
            eventPublisher.publishEvent(new ItemAvaliadoEvent(this, item.getId(), item.getOsId(), dto.getResultado()));
            // Conflito só faz sentido quando NÃO foi confirmado como a própria peça coberta —
            // aí é um defeito diferente num equipamento que ainda tem outra garantia ativa.
            if (!item.isGarantia() && item.getItemEstoqueId() != null
                    && !garantiaPecaDomainService.listarCoberturaAtiva(item.getItemEstoqueId()).isEmpty()) {
                eventPublisher.publishEvent(
                        new GarantiaConflitoEvent(this, item.getId(), item.getOsId(), item.getItemEstoqueId()));
            }
        }
        return comCatalogo(mapper.toDTO(item));
    }

    /**
     * Edita o laudo já salvo (todos os campos) sem repetir os efeitos
     * colaterais da primeira vez — não muda o status nem publica os eventos
     * de orçamento/garantia de novo, só corrige o texto/decisão registrada.
     */
    @Transactional
    public ItemEntradaDTO atualizarAvaliacaoCompleta(UUID id, SalvarAvaliacaoTecnicaDTO dto) {
        ItemEntrada item = itemDomainService.atualizarAvaliacaoCompleta(id, dto.getResultado(), dto.getDetalheAjuste(),
                dto.getDefeitoEncontrado(), dto.getCausaDefeito(), dto.getSolucaoRecomendada(), dto.getObservacoesTecnicas(),
                dto.getGarantiaPecaIds());
        sugestaoTextoService.registrarUso(CampoSugestao.DEFEITO_ENCONTRADO, dto.getDefeitoEncontrado());
        sugestaoTextoService.registrarUso(CampoSugestao.CAUSA_DEFEITO, dto.getCausaDefeito());
        sugestaoTextoService.registrarUso(CampoSugestao.SOLUCAO_RECOMENDADA, dto.getSolucaoRecomendada());
        sugestaoTextoService.registrarUso(CampoSugestao.OBSERVACOES_TECNICAS, dto.getObservacoesTecnicas());
        return comCatalogo(mapper.toDTO(item));
    }

    /** Peças/equipamentos com cobertura de garantia ativa desse item — pro seletor da tela de avaliação. */
    @Transactional(readOnly = true)
    public List<GarantiaPecaDTO> listarGarantiaDisponivel(UUID id) {
        return itemDomainService.listarGarantiaDisponivel(id).stream()
                .map(this::toGarantiaPecaDTO)
                .collect(Collectors.toList());
    }

    private GarantiaPecaDTO toGarantiaPecaDTO(GarantiaPeca g) {
        return GarantiaPecaDTO.builder()
                .id(g.getId())
                .pecaEstoqueId(g.getPecaEstoqueId())
                .descricaoPeca(g.getDescricaoPeca())
                .dataInicio(g.getDataInicio())
                .dataFim(g.getDataFim())
                .build();
    }

    @Transactional
    public ItemEntradaDTO confirmarAguardandoPeca(UUID id) {
        return comCatalogo(mapper.toDTO(itemDomainService.confirmarAguardandoPeca(id)));
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

    /** Define o valor de uma peça escolhida pelo técnico na avaliação (sem preço) — usado no orçamento. */
    @Transactional
    public ItemEntradaDTO atualizarValorItemConserto(UUID id, UUID itemConsertoId, AtualizarValorItemConsertoDTO dto) {
        return comCatalogo(mapper.toDTO(
                itemDomainService.atualizarValorItemConserto(id, itemConsertoId, dto.getValorUnitario())));
    }

    @Transactional
    public void remover(UUID id) {
        itemDomainService.remover(id);
        log.info("Item de entrada removido: {}", id);
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
