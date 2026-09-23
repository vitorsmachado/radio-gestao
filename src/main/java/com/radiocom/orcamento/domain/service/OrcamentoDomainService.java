package com.radiocom.orcamento.domain.service;

import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import com.radiocom.orcamento.domain.repository.OrcamentoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoDomainService {

    private final OrcamentoRepository orcamentoRepository;
    private final ItemEntradaRepository itemEntradaRepository;
    private final ItemEntradaDomainService itemEntradaDomainService;
    private final NumeroOrcamentoGenerator numeroGenerator;

    @Transactional
    public Orcamento criar(UUID osId, UUID clienteId, LocalDate validade,
                            String condicoesPagamento, BigDecimal desconto) {
        Orcamento orcamento = Orcamento.builder()
                .numero(numeroGenerator.gerarNumero())
                .osId(osId)
                .clienteId(clienteId)
                .validade(validade)
                .condicoesPagamento(condicoesPagamento)
                .desconto(desconto != null ? desconto : BigDecimal.ZERO)
                .build();
        return orcamentoRepository.save(orcamento);
    }

    @Transactional(readOnly = true)
    public Orcamento buscarPorId(UUID id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> new DomainException("Orçamento não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Orcamento buscarPorNumero(String numero) {
        return orcamentoRepository.findByNumero(numero)
                .orElseThrow(() -> new DomainException("Orçamento não encontrado: " + numero));
    }

    @Transactional(readOnly = true)
    public List<Orcamento> listarPorOS(UUID osId) {
        return orcamentoRepository.findByOsId(osId);
    }

    @Transactional(readOnly = true)
    public Page<Orcamento> buscar(String busca, List<UUID> osIdsMatched, List<UUID> clienteIdsMatched,
                                   StatusOrcamento status, Pageable pageable) {
        return orcamentoRepository.buscar(busca, osIdsMatched, clienteIdsMatched, status, pageable);
    }

    @Transactional
    public Orcamento atualizar(UUID id, LocalDate validade, String condicoesPagamento, BigDecimal desconto) {
        Orcamento orcamento = buscarPorId(id);
        orcamento.atualizarCondicoes(validade, condicoesPagamento, desconto);
        return orcamentoRepository.save(orcamento);
    }

    /**
     * Encontra o orçamento RASCUNHO já aberto pra essa OS, ou cria um novo
     * (sem validade/condições — o admin ajusta depois). Usado pela geração
     * automática ao final de cada avaliação técnica.
     */
    @Transactional
    public Orcamento buscarOuCriarRascunho(UUID osId, UUID clienteId) {
        Optional<Orcamento> existente = orcamentoRepository.findByOsId(osId).stream()
                .filter(o -> o.getStatus() == StatusOrcamento.RASCUNHO)
                .findFirst();
        return existente.orElseGet(() -> criar(osId, clienteId, null, null, null));
    }

    // ===== ITENS DO ORÇAMENTO =====

    @Transactional(readOnly = true)
    public List<ItemEntrada> listarItens(UUID orcamentoId) {
        return itemEntradaRepository.findByOrcamentoId(orcamentoId);
    }

    @Transactional
    public void adicionarItem(UUID orcamentoId, UUID itemEntradaId) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        ItemEntrada item = buscarItem(itemEntradaId);
        if (!item.getOsId().equals(orcamento.getOsId())) {
            throw new DomainException("Item não pertence à mesma OS do orçamento");
        }
        item.atribuirOrcamento(orcamentoId);
        itemEntradaRepository.save(item);
    }

    @Transactional
    public void removerItem(UUID orcamentoId, UUID itemEntradaId) {
        ItemEntrada item = buscarItem(itemEntradaId);
        if (!orcamentoId.equals(item.getOrcamentoId())) {
            throw new DomainException("Item não pertence a este orçamento");
        }
        item.removerDoOrcamento();
        itemEntradaRepository.save(item);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(UUID orcamentoId) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        BigDecimal totalItens = listarItens(orcamentoId).stream()
                .map(ItemEntrada::calcularTotalConserto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalItens.subtract(orcamento.getDesconto()).max(BigDecimal.ZERO);
    }

    /**
     * Classificação agregada de aprovação, calculada ao vivo a partir do
     * status de cada item agrupado — nunca persistida. Itens sem defeito
     * (que nunca passam por autorização) não entram na conta.
     */
    @Transactional(readOnly = true)
    public StatusAprovacaoOrcamento calcularStatusAprovacao(UUID orcamentoId) {
        List<ItemEntrada> relevantes = listarItens(orcamentoId).stream()
                .filter(item -> !item.isSemDefeito())
                .collect(Collectors.toList());

        if (relevantes.isEmpty()) {
            return StatusAprovacaoOrcamento.PENDENTE;
        }

        long total = relevantes.size();
        long autorizados = relevantes.stream().filter(this::isAutorizado).count();
        long naoAutorizados = relevantes.stream().filter(this::isNaoAutorizado).count();

        if (autorizados == total) return StatusAprovacaoOrcamento.AUTORIZADO;
        if (naoAutorizados == total) return StatusAprovacaoOrcamento.NAO_AUTORIZADO;
        if (autorizados > 0 || naoAutorizados > 0) return StatusAprovacaoOrcamento.PARCIALMENTE_AUTORIZADO;
        return StatusAprovacaoOrcamento.PENDENTE;
    }

    /**
     * NAO_AUTORIZADO some do status assim que o item vai pra AGUARDANDO_ENTREGA
     * — por isso, além do status atual, usa o motivoNaoAutorizado (só é limpo
     * de novo se o item for reautorizado) como marca de que foi rejeitado.
     */
    private boolean isNaoAutorizado(ItemEntrada item) {
        if (item.getStatus() == StatusItemEntrada.NAO_AUTORIZADO) {
            return true;
        }
        boolean entregaFinal = item.getStatus() == StatusItemEntrada.AGUARDANDO_ENTREGA
                || item.getStatus() == StatusItemEntrada.ENTREGUE;
        return entregaFinal && item.getMotivoNaoAutorizado() != null;
    }

    private boolean isPendente(ItemEntrada item) {
        return item.getStatus() == StatusItemEntrada.PENDENTE_AVALIACAO
                || item.getStatus() == StatusItemEntrada.AVALIADO
                || item.getStatus() == StatusItemEntrada.PENDENTE_AUTORIZACAO;
    }

    private boolean isAutorizado(ItemEntrada item) {
        return !isPendente(item) && !isNaoAutorizado(item);
    }

    // ===== TRANSIÇÕES =====

    /**
     * Envia o orçamento ao cliente: todo item ainda AVALIADO passa para
     * PENDENTE_AUTORIZACAO (itens já enviados individualmente antes não são
     * afetados de novo).
     */
    @Transactional
    public Orcamento enviar(UUID orcamentoId) {
        Orcamento orcamento = buscarPorId(orcamentoId);
        List<ItemEntrada> itens = listarItens(orcamentoId);
        if (itens.isEmpty()) {
            throw new DomainException("Selecione ao menos um item para enviar o orçamento");
        }
        itens.stream()
                .filter(item -> item.getStatus() == StatusItemEntrada.AVALIADO)
                .forEach(item -> itemEntradaDomainService.enviarParaAutorizacao(item.getId()));
        orcamento.enviar();
        return orcamentoRepository.save(orcamento);
    }

    @Transactional
    public Orcamento cancelar(UUID id, String motivo) {
        Orcamento orcamento = buscarPorId(id);
        orcamento.cancelar(motivo);
        return orcamentoRepository.save(orcamento);
    }

    private ItemEntrada buscarItem(UUID itemEntradaId) {
        return itemEntradaRepository.findById(itemEntradaId)
                .orElseThrow(() -> new DomainException("Item de entrada não encontrado: " + itemEntradaId));
    }
}
