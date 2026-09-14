package com.radiocom.estoque.domain.service;

import com.radiocom.estoque.domain.model.Acessorio;
import com.radiocom.estoque.domain.model.ItemEstoque;
import com.radiocom.estoque.domain.model.MovimentacaoEstoque;
import com.radiocom.estoque.domain.model.Peca;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.model.enums.TipoMovimentacao;
import com.radiocom.estoque.domain.repository.AcessorioRepository;
import com.radiocom.estoque.domain.repository.MovimentacaoEstoqueRepository;
import com.radiocom.estoque.domain.repository.PecaRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Movimentação de estoque para itens controlados por quantidade (Acessório e Peça).
 * Equipamentos são rastreados individualmente por N/S — não aceitam movimentação
 * por quantidade, use {@link EquipamentoDomainService} para suas transições de estado.
 */
@Service
@RequiredArgsConstructor
public class EstoqueDomainService {

    private final AcessorioRepository acessorioRepository;
    private final PecaRepository pecaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional(readOnly = true)
    public Acessorio buscarAcessorioPorId(UUID id) {
        return acessorioRepository.findById(id)
                .orElseThrow(() -> new DomainException("Acessório não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Peca buscarPecaPorId(UUID id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new DomainException("Peça não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public Integer consultarSaldo(UUID itemId, TipoItem tipoItem) {
        return buscarItem(itemId, tipoItem).getQuantidadeDisponivel();
    }

    @Transactional(readOnly = true)
    public boolean verificarDisponibilidade(UUID itemId, TipoItem tipoItem, Integer quantidade) {
        try {
            return buscarItem(itemId, tipoItem).getQuantidadeDisponivel() >= quantidade;
        } catch (DomainException e) {
            return false;
        }
    }

    @Transactional
    public Integer darEntrada(UUID itemId, TipoItem tipoItem, Integer quantidade, String motivo) {
        ItemEstoque item = buscarItem(itemId, tipoItem);
        int saldoAnterior = item.getQuantidadeDisponivel();
        item.entrada(quantidade);
        salvarItem(item, tipoItem);
        registrarMovimentacao(itemId, tipoItem, TipoMovimentacao.ENTRADA, saldoAnterior, item.getQuantidadeDisponivel(), motivo);
        return item.getQuantidadeDisponivel();
    }

    @Transactional
    public Integer darSaida(UUID itemId, TipoItem tipoItem, Integer quantidade, String motivo) {
        ItemEstoque item = buscarItem(itemId, tipoItem);
        int saldoAnterior = item.getQuantidadeDisponivel();
        item.saida(quantidade);
        salvarItem(item, tipoItem);
        registrarMovimentacao(itemId, tipoItem, TipoMovimentacao.SAIDA, saldoAnterior, item.getQuantidadeDisponivel(), motivo);
        return item.getQuantidadeDisponivel();
    }

    @Transactional
    public Integer ajustar(UUID itemId, TipoItem tipoItem, Integer novaQuantidade, String motivo) {
        ItemEstoque item = buscarItem(itemId, tipoItem);
        int saldoAnterior = item.getQuantidadeDisponivel();
        item.ajustar(novaQuantidade);
        salvarItem(item, tipoItem);
        registrarMovimentacao(itemId, tipoItem, TipoMovimentacao.AJUSTE, saldoAnterior, item.getQuantidadeDisponivel(), motivo);
        return item.getQuantidadeDisponivel();
    }

    private void registrarMovimentacao(UUID itemId, TipoItem tipoItem, TipoMovimentacao tipoMovimentacao,
                                        int saldoAnterior, int saldoNovo, String motivo) {
        movimentacaoRepository.save(MovimentacaoEstoque.builder()
                .itemId(itemId)
                .tipoItem(tipoItem)
                .tipoMovimentacao(tipoMovimentacao)
                .saldoAnterior(saldoAnterior)
                .saldoNovo(saldoNovo)
                .motivo(motivo != null && !motivo.isBlank() ? motivo.trim() : null)
                .build());
    }

    private ItemEstoque buscarItem(UUID id, TipoItem tipo) {
        return switch (tipo) {
            case ACESSORIO -> buscarAcessorioPorId(id);
            case PECA -> buscarPecaPorId(id);
            case EQUIPAMENTO -> throw new DomainException(
                    "Equipamentos são rastreados por N/S. Use EquipamentoDomainService.");
            case SERVICO -> throw new DomainException(
                    "Serviços não possuem estoque físico e não aceitam movimentação.");
        };
    }

    private void salvarItem(ItemEstoque item, TipoItem tipo) {
        switch (tipo) {
            case ACESSORIO -> acessorioRepository.save((Acessorio) item);
            case PECA -> pecaRepository.save((Peca) item);
            default -> throw new DomainException("Tipo de item não movimentável: " + tipo);
        }
    }
}
