package com.radiocom.orcamento.application.service;

import com.radiocom.cliente.application.dto.ClienteDTO;
import com.radiocom.cliente.application.service.ClienteApplicationService;
import com.radiocom.estoque.application.service.CatalogoModeloService;
import com.radiocom.ordemservico.application.dto.ItemConsertoDTO;
import com.radiocom.ordemservico.application.dto.ItemEntradaDTO;
import com.radiocom.ordemservico.application.dto.MotivoDTO;
import com.radiocom.ordemservico.application.mapper.OrdemServicoMapper;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
import com.radiocom.orcamento.application.dto.AdicionarItemOrcamentoDTO;
import com.radiocom.orcamento.application.dto.AtualizarOrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoCreateDTO;
import com.radiocom.orcamento.application.dto.OrcamentoDTO;
import com.radiocom.orcamento.application.dto.OrcamentoResumoDTO;
import com.radiocom.orcamento.application.mapper.OrcamentoMapper;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrcamentoApplicationService {

    /** UUID sentinela — usado no lugar de uma lista vazia pra evitar "IN ()" no SQL quando nada casa com a busca. */
    private static final UUID ID_INEXISTENTE = new UUID(0, 0);
    private static final int RESUMO_ITENS_MAX = 3;

    private final OrcamentoDomainService orcamentoDomainService;
    private final OrdemServicoDomainService osDomainService;
    private final ClienteApplicationService clienteApplicationService;
    private final CatalogoModeloService catalogoModeloService;
    private final GarantiaPecaDomainService garantiaPecaDomainService;
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

    @Transactional
    public OrcamentoDTO atualizar(UUID id, AtualizarOrcamentoDTO dto) {
        Orcamento orcamento = orcamentoDomainService.atualizar(
                id, dto.getValidade(), dto.getCondicoesPagamento(), dto.getDesconto());
        return toDTOComItens(orcamento);
    }

    @Transactional(readOnly = true)
    public Page<OrcamentoResumoDTO> listar(String busca, StatusOrcamento status, Pageable pageable) {
        String buscaTratada = busca != null && !busca.isBlank() ? busca.trim() : null;

        List<UUID> osIdsMatched = List.of(ID_INEXISTENTE);
        List<UUID> clienteIdsMatched = List.of(ID_INEXISTENTE);
        if (buscaTratada != null) {
            List<UUID> osEncontradas = osDomainService.buscarIdsPorNumero(buscaTratada);
            if (!osEncontradas.isEmpty()) osIdsMatched = osEncontradas;

            List<UUID> clientesEncontrados = clienteApplicationService.buscarPorNomeOuDocumento(buscaTratada).stream()
                    .map(ClienteDTO::getId)
                    .collect(Collectors.toList());
            if (!clientesEncontrados.isEmpty()) clienteIdsMatched = clientesEncontrados;
        }

        Page<Orcamento> pagina = orcamentoDomainService.buscar(buscaTratada, osIdsMatched, clienteIdsMatched, status, pageable);

        List<UUID> osIdsDaPagina = pagina.getContent().stream().map(Orcamento::getOsId).distinct().collect(Collectors.toList());
        Map<UUID, String> osNumeroPorId = osIdsDaPagina.isEmpty() ? Map.of()
                : osDomainService.listarPorIds(osIdsDaPagina).stream()
                        .collect(Collectors.toMap(os -> os.getId(), os -> os.getNumero()));

        List<UUID> clienteIdsDaPagina = pagina.getContent().stream().map(Orcamento::getClienteId).distinct().collect(Collectors.toList());
        Map<UUID, ClienteDTO> clientesPorId = clienteIdsDaPagina.isEmpty() ? Map.of()
                : clienteApplicationService.buscarPorIds(clienteIdsDaPagina).stream()
                        .collect(Collectors.toMap(ClienteDTO::getId, Function.identity()));

        return pagina.map(orcamento -> toResumoDTO(orcamento, osNumeroPorId, clientesPorId));
    }

    private OrcamentoResumoDTO toResumoDTO(Orcamento orcamento, Map<UUID, String> osNumeroPorId, Map<UUID, ClienteDTO> clientesPorId) {
        List<ItemEntrada> itens = orcamentoDomainService.listarItens(orcamento.getId());
        ClienteDTO cliente = clientesPorId.get(orcamento.getClienteId());
        List<String> descricoes = itens.stream().map(ItemEntrada::getDescricao).collect(Collectors.toList());
        String resumoItens = descricoes.isEmpty() ? "—"
                : descricoes.size() <= RESUMO_ITENS_MAX
                        ? String.join(", ", descricoes)
                        : String.join(", ", descricoes.subList(0, RESUMO_ITENS_MAX)) + " (+" + (descricoes.size() - RESUMO_ITENS_MAX) + ")";

        return OrcamentoResumoDTO.builder()
                .id(orcamento.getId())
                .numero(orcamento.getNumero())
                .osId(orcamento.getOsId())
                .osNumero(osNumeroPorId.get(orcamento.getOsId()))
                .clienteId(orcamento.getClienteId())
                .clienteNome(cliente != null ? cliente.getNomeRazaoSocial() : null)
                .status(orcamento.getStatus())
                .statusAprovacao(orcamentoDomainService.calcularStatusAprovacao(orcamento.getId()))
                .dataEmissao(orcamento.getDataEmissao())
                .validade(orcamento.getValidade())
                .expirado(orcamento.isExpirado())
                .valorTotal(orcamentoDomainService.calcularTotal(orcamento.getId()))
                .quantidadeItens(itens.size())
                .resumoItens(resumoItens)
                .build();
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
        List<ItemEntradaDTO> itens = itemMapper.toItemDTOList(orcamentoDomainService.listarItens(orcamento.getId()));
        dto.setItens(comGarantia(comCatalogo(itens)));
        dto.setValorTotal(orcamentoDomainService.calcularTotal(orcamento.getId()));
        dto.setStatusAprovacao(orcamentoDomainService.calcularStatusAprovacao(orcamento.getId()));
        return dto;
    }

    /** Resolve o valor de referência do catálogo (útil sobretudo pros itens marcados "sem conserto"). */
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

    /** Marca, em cada peça do conserto, se ela tem cobertura de garantia ativa agora — só informativo pro admin decidir se cobra. */
    private List<ItemEntradaDTO> comGarantia(List<ItemEntradaDTO> dtos) {
        for (ItemEntradaDTO dto : dtos) {
            if (dto.getItemEstoqueId() == null || dto.getItensConserto() == null) continue;
            var pecasCobertas = garantiaPecaDomainService.listarCoberturaAtiva(dto.getItemEstoqueId()).stream()
                    .map(g -> g.getPecaEstoqueId())
                    .collect(Collectors.toSet());
            if (pecasCobertas.isEmpty()) continue;
            for (ItemConsertoDTO conserto : dto.getItensConserto()) {
                if (conserto.getTipo() == TipoItemConserto.PECA && pecasCobertas.contains(conserto.getItemEstoqueId())) {
                    conserto.setCoberto(true);
                }
            }
        }
        return dtos;
    }
}
