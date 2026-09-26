package com.radiocom.ordemservico.domain.service;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.estoque.domain.service.EstoqueDomainService;
import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.ItemEntradaStatusHistorico;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.repository.ItemEntradaStatusHistoricoRepository;
import com.radiocom.ordemservico.garantia.domain.model.GarantiaPeca;
import com.radiocom.ordemservico.garantia.domain.service.GarantiaPecaDomainService;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemEntradaDomainService {

    private final ItemEntradaRepository itemEntradaRepository;
    private final ItemEntradaStatusHistoricoRepository statusHistoricoRepository;
    private final EstoqueDomainService estoqueDomainService;
    private final GarantiaPecaDomainService garantiaPecaDomainService;

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
        validarQuantidade(item);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada avaliar(UUID id, String avaliacaoTecnica, boolean semDefeito) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.avaliar(avaliacaoTecnica, semDefeito);
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    @Transactional
    public ItemEntrada atualizarAvaliacao(UUID id, String avaliacaoTecnica, boolean semDefeito) {
        ItemEntrada item = buscarPorId(id);
        item.atualizarAvaliacao(avaliacaoTecnica, semDefeito);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada iniciarAvaliacao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.iniciarAvaliacao();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    @Transactional
    public ItemEntrada salvarAvaliacaoTecnica(UUID id, ResultadoAvaliacao resultado, String detalheAjuste,
                                               String defeitoEncontrado, String causaDefeito,
                                               String solucaoRecomendada, String observacoesTecnicas,
                                               List<UUID> garantiaPecaIds) {
        ItemEntrada item = buscarPorId(id);
        Set<UUID> pecasCobertas = validarGarantiaMultipla(item, garantiaPecaIds);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.salvarAvaliacaoTecnica(resultado, detalheAjuste, defeitoEncontrado, causaDefeito,
                solucaoRecomendada, observacoesTecnicas, !pecasCobertas.isEmpty(), pecasCobertas);
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        // Foi direto pra aguardando entrega porque toda peça já era garantia — baixa do estoque agora,
        // já que esse item nunca vai passar por EM_MANUTENCAO/concluirManutencao pra baixar lá.
        if (salvo.getStatus() == StatusItemEntrada.AGUARDANDO_ENTREGA && salvo.isGarantia()) {
            baixarEstoquePecasUsadas(salvo);
        }
        return salvo;
    }

    /**
     * Edita o laudo já salvo sem mexer no status atual do item — usado pelo
     * botão "atualizar avaliação" depois que o laudo inicial já progrediu
     * pra outro status (autorizado, em manutenção etc.).
     */
    @Transactional
    public ItemEntrada atualizarAvaliacaoCompleta(UUID id, ResultadoAvaliacao resultado, String detalheAjuste,
                                                   String defeitoEncontrado, String causaDefeito,
                                                   String solucaoRecomendada, String observacoesTecnicas,
                                                   List<UUID> garantiaPecaIds) {
        ItemEntrada item = buscarPorId(id);
        Set<UUID> pecasCobertas = validarGarantiaMultipla(item, garantiaPecaIds);
        item.atualizarAvaliacaoCompleta(resultado, detalheAjuste, defeitoEncontrado, causaDefeito,
                solucaoRecomendada, observacoesTecnicas, !pecasCobertas.isEmpty());
        return itemEntradaRepository.save(item);
    }

    /** Valida cada cobertura reivindicada e devolve o conjunto de itemEstoqueId de peça que ela cobre. */
    private Set<UUID> validarGarantiaMultipla(ItemEntrada item, List<UUID> garantiaPecaIds) {
        if (garantiaPecaIds == null || garantiaPecaIds.isEmpty()) return Set.of();
        if (item.getItemEstoqueId() == null) {
            throw new DomainException(
                    "Item não está vinculado a um equipamento/acessório do cliente — não é possível aplicar garantia.");
        }
        Set<UUID> pecasCobertas = new java.util.HashSet<>();
        for (UUID garantiaPecaId : garantiaPecaIds) {
            var cobertura = garantiaPecaDomainService.validarCoberturaAtiva(item.getItemEstoqueId(), garantiaPecaId);
            pecasCobertas.add(cobertura.getPecaEstoqueId());
        }
        return pecasCobertas;
    }

    /** Peças/equipamentos com cobertura de garantia ativa desse item — vazio se não rastreado ou sem cobertura. */
    @Transactional(readOnly = true)
    public List<GarantiaPeca> listarGarantiaDisponivel(UUID id) {
        ItemEntrada item = buscarPorId(id);
        if (item.getItemEstoqueId() == null) return List.of();
        return garantiaPecaDomainService.listarCoberturaAtiva(item.getItemEstoqueId());
    }

    /** Não muda status — só marca o timestamp que a fila de manutenção usa pra ordenar. */
    @Transactional
    public ItemEntrada confirmarAguardandoPeca(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.confirmarAguardandoPeca();
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada enviarParaAutorizacao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.enviarParaAutorizacao();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    /**
     * Autoriza o conserto e decide sozinho o próximo passo: se todas as
     * peças do conserto estão disponíveis em estoque, o item já entra na
     * fila de manutenção; se faltar alguma, vai direto para aguardando peça.
     * Cada sub-transição (autorizar, e depois fila/aguardando peça) vira um
     * registro de histórico próprio.
     */
    @Transactional
    public ItemEntrada autorizar(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.autorizar();
        registrarTransicaoStatus(id, statusAnterior, item.getStatus(), null);

        StatusItemEntrada antesDaFila = item.getStatus();
        if (pecasDisponiveis(item)) {
            item.iniciarFilaManutencao();
        } else {
            item.marcarAguardandoPeca();
        }
        registrarTransicaoStatus(id, antesDaFila, item.getStatus(), null);

        return itemEntradaRepository.save(item);
    }

    @Transactional
    public ItemEntrada naoAutorizar(UUID id, String motivo) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.naoAutorizar(motivo);
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), motivo);
        return salvo;
    }

    @Transactional
    public ItemEntrada iniciarManutencao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.iniciarManutencao();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    /**
     * Uso manual pelo técnico ao descobrir, no meio do reparo, que falta
     * uma peça — distinto de {@link #autorizar} que já decide isso sozinho
     * no momento da autorização.
     */
    @Transactional
    public ItemEntrada marcarAguardandoPeca(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.marcarAguardandoPeca();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    @Transactional
    public ItemEntrada concluirManutencao(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.concluirManutencao();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        garantiaPecaDomainService.registrarCobertura(salvo);
        baixarEstoquePecasUsadas(salvo);
        return salvo;
    }

    /** Desconta do estoque cada peça efetivamente usada no reparo — só acontece aqui, ao concluir de verdade. */
    private void baixarEstoquePecasUsadas(ItemEntrada item) {
        for (ItemConserto conserto : item.getItensConserto()) {
            if (conserto.getTipo() != TipoItemConserto.PECA || conserto.getItemEstoqueId() == null) continue;
            estoqueDomainService.darSaida(conserto.getItemEstoqueId(), TipoItem.PECA, conserto.getQuantidade(),
                    "Consumido no conserto: " + item.getDescricao());
        }
    }

    @Transactional
    public ItemEntrada aguardarEntrega(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.aguardarEntrega();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    @Transactional
    public ItemEntrada entregar(UUID id) {
        ItemEntrada item = buscarPorId(id);
        StatusItemEntrada statusAnterior = item.getStatus();
        item.entregar();
        ItemEntrada salvo = itemEntradaRepository.save(item);
        registrarTransicaoStatus(id, statusAnterior, salvo.getStatus(), null);
        return salvo;
    }

    /**
     * Chamado quando uma peça recebe entrada no estoque — todo item que
     * estava aguardando exatamente essa peça e já tem quantidade suficiente
     * volta pra fila de manutenção automaticamente.
     */
    @Transactional
    public void retomarItensAguardandoPeca(UUID itemEstoqueId) {
        List<ItemEntrada> aguardando = itemEntradaRepository.findByStatusAndItemEstoqueId(
                StatusItemEntrada.AGUARDANDO_PECA, itemEstoqueId);

        for (ItemEntrada item : aguardando) {
            if (pecasDisponiveis(item)) {
                StatusItemEntrada statusAnterior = item.getStatus();
                item.retomarAposPeca();
                itemEntradaRepository.save(item);
                registrarTransicaoStatus(item.getId(), statusAnterior, item.getStatus(), null);
            }
        }
    }

    private void registrarTransicaoStatus(UUID itemEntradaId, StatusItemEntrada statusAnterior,
                                           StatusItemEntrada statusNovo, String motivo) {
        statusHistoricoRepository.save(ItemEntradaStatusHistorico.builder()
                .itemEntradaId(itemEntradaId)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .motivo(motivo != null && !motivo.isBlank() ? motivo.trim() : null)
                .build());
    }

    private void validarQuantidade(ItemEntrada item) {
        int quantidade = item.getQuantidade() != null ? item.getQuantidade() : 1;
        boolean rastreado = (item.getNumeroSerie() != null && !item.getNumeroSerie().isBlank())
                || (item.getPatrimonio() != null && !item.getPatrimonio().isBlank());
        if (quantidade > 1 && rastreado) {
            throw new DomainException(
                    "Item com quantidade maior que 1 não pode ter número de série nem patrimônio — "
                            + "use uma linha por unidade rastreada.");
        }
        if (quantidade < 1) {
            throw new DomainException("Quantidade deve ser maior ou igual a 1.");
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

    @Transactional
    public ItemEntrada atualizarValorItemConserto(UUID id, UUID itemConsertoId, BigDecimal valorUnitario) {
        ItemEntrada item = buscarPorId(id);
        item.atualizarValorItemConserto(itemConsertoId, valorUnitario);
        return itemEntradaRepository.save(item);
    }

    @Transactional
    public void remover(UUID id) {
        ItemEntrada item = buscarPorId(id);
        item.validarRemocao();
        itemEntradaRepository.delete(item);
    }

    private boolean pecasDisponiveis(ItemEntrada item) {
        return item.getItensConserto().stream()
                .filter(ic -> ic.getTipo() == TipoItemConserto.PECA && ic.getItemEstoqueId() != null)
                .allMatch(ic -> estoqueDomainService.verificarDisponibilidade(
                        ic.getItemEstoqueId(), TipoItem.PECA, ic.getQuantidade()));
    }
}