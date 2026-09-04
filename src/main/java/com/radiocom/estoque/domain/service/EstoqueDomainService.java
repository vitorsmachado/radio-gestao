package com.radiocom.estoque.domain.service;

import com.radiocom.estoque.domain.model.Acessorio;
import com.radiocom.estoque.domain.model.ItemEstoque;
import com.radiocom.estoque.domain.model.Peca;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.repository.AcessorioRepository;
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
    public Integer darEntrada(UUID itemId, TipoItem tipoItem, Integer quantidade) {
        ItemEstoque item = buscarItem(itemId, tipoItem);
        item.entrada(quantidade);
        salvarItem(item, tipoItem);
        return item.getQuantidadeDisponivel();
    }

    @Transactional
    public Integer darSaida(UUID itemId, TipoItem tipoItem, Integer quantidade) {
        ItemEstoque item = buscarItem(itemId, tipoItem);
        item.saida(quantidade);
        salvarItem(item, tipoItem);
        return item.getQuantidadeDisponivel();
    }

    @Transactional
    public Integer ajustar(UUID itemId, TipoItem tipoItem, Integer novaQuantidade) {
        ItemEstoque item = buscarItem(itemId, tipoItem);
        item.ajustar(novaQuantidade);
        salvarItem(item, tipoItem);
        return item.getQuantidadeDisponivel();
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
