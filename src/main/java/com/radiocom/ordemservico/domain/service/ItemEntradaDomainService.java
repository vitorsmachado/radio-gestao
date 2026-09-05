package com.radiocom.ordemservico.domain.service;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.service.EstoqueDomainService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemEntradaDomainService {

    private final ItemEntradaRepository itemEntradaRepository;
    private final EstoqueDomainService estoqueDomainService;

    @Transactional(readOnly = true)
    public ItemEntrada buscarPorId(UUID id) {
        return itemEntradaRepository.findById(id)
                .orElseThrow(() -> new DomainException("Item de entrada não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<ItemEntrada> listarPorOS(UUID osId) {
        return itemEntradaRepository.findByOsId(osId);
    }

    @Transactional
    public ItemEntrada criar(ItemEntrada item) {
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada avaliar(UUID id, String avaliacaoTecnica, boolean semDefeito) {
        ItemEntrada item = buscarPorId(id);
        item.avaliar(avaliacaoTecnica, semDefeito);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada atualizarAvaliacao(UUID id, String avaliacaoTecnica, boolean semDefeito) {
        ItemEntrada item = buscarPorId(id);
        item.atualizarAvaliacao(avaliacaoTecnica, semDefeito);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada enviarParaAutorizacao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.enviarParaAutorizacao();
        return itemEntradaRepository.save(item);
    }

    /**
     * Autoriza o conserto e decide sozinho o próximo passo: se todas as
     * peças do conserto estão disponíveis em estoque, o item já entra na
     * fila de manutenção; se faltar alguma, vai direto para aguardando peça.
     */
    @Transactional
    public ItemEntrada autorizar(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.autorizar();

        if (pecasDisponiveis(item)) {
            item.iniciarFilaManutencao();
        } else {
            item.marcarAguardandoPeca();
        }

        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada naoAutorizar(UUID id, String motivo) {
        ItemEntrada item = buscarPorId(id);
        item.naoAutorizar(motivo);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada iniciarManutencao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.iniciarManutencao();
        return itemEntradaRepository.save(item);
    }

    /**
     * Uso manual pelo técnico ao descobrir, no meio do reparo, que falta
     * uma peça — distinto de {@link #autorizar} que já decide isso sozinho
     * no momento da autorização.
     */
    @Transactional
    public ItemEntrada marcarAguardandoPeca(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.marcarAguardandoPeca();
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada concluirManutencao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.concluirManutencao();
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada aguardarEntrega(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.aguardarEntrega();
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada entregar(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.entregar();
        return itemEntradaRepository.save(item);
    }

    /**
     * Chamado quando uma peça recebe entrada no estoque — todo item que
     * estava aguardando exatamente essa peça e já tem quantidade suficiente
     * volta pra fila de manutenção automaticamente.
     */
    @Transactional
    public void retomarItensAguardandoPeca(UUID itemEstoqueId) {
        List<ItemEntrada> aguardando = itemEntradaRepository.findByStatusAndItemEstoqueId(
                com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada.AGUARDANDO_PECA, itemEstoqueId);

        for (ItemEntrada item : aguardando) {
            if (pecasDisponiveis(item)) {
                item.retomarAposPeca();
                itemEntradaRepository.save(item);
            }
        }
    }

    @Transactional
    public ItemEntrada adicionarItemConserto(UUID id, ItemConserto itemConserto) {
        ItemEntrada item = buscarPorId(id);
        item.adicionarItemConserto(itemConserto);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada removerItemConserto(UUID id, UUID itemConsertoId) {
        ItemEntrada item = buscarPorId(id);
        item.removerItemConserto(itemConsertoId);
        return itemEntradaRepository.save(item);
    }

    private boolean pecasDisponiveis(ItemEntrada item) {
        return item.getItensConserto().stream()
                .filter(ic -> ic.getTipo() == TipoItemConserto.PECA && ic.getItemEstoqueId() != null)
                .allMatch(ic -> estoqueDomainService.verificarDisponibilidade(
                        ic.getItemEstoqueId(), TipoItem.PECA, ic.getQuantidade()));
    }
}
