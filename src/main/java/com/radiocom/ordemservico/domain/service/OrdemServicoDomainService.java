package com.radiocom.ordemservico.domain.service;

import com.radiocom.ordemservico.domain.model.ItemEntrada;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.OrdemServicoStatusHistorico;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import com.radiocom.ordemservico.domain.model.enums.StatusOS;
import com.radiocom.ordemservico.domain.repository.ItemEntradaRepository;
import com.radiocom.ordemservico.domain.repository.OrdemServicoRepository;
import com.radiocom.ordemservico.domain.repository.OrdemServicoStatusHistoricoRepository;
import com.radiocom.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoDomainService {

    private final OrdemServicoRepository osRepository;
    private final ItemEntradaRepository itemEntradaRepository;
    private final OrdemServicoStatusHistoricoRepository statusHistoricoRepository;
    private final NumeroOSGenerator numeroGenerator;

    @Transactional(readOnly = true)
    public OrdemServico buscarPorId(UUID id) {
        return osRepository.findById(id)
                .orElseThrow(() -> new DomainException("Ordem de Serviço não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarPorNumero(String numero) {
        return osRepository.findByNumero(numero)
                .orElseThrow(() -> new DomainException("OS não encontrada: " + numero));
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarPorCliente(UUID clienteId) {
        return osRepository.findByClienteIdOrderByDataAberturaDesc(clienteId);
    }

    /** Usado pela aba Garantia do cliente — todas as passagens de um item por uma OS. */
    @Transactional(readOnly = true)
    public List<ItemEntrada> listarItensPorItemEstoque(UUID itemEstoqueId) {
        return itemEntradaRepository.findByItemEstoqueIdOrderByDataCriacaoDesc(itemEstoqueId);
    }

    /** Usado pela fila de manutenção do técnico. */
    @Transactional(readOnly = true)
    public List<ItemEntrada> listarItensNaFilaManutencao() {
        return itemEntradaRepository.findByStatusIn(List.of(
                StatusItemEntrada.EM_AVALIACAO, StatusItemEntrada.PENDENTE_AVALIACAO,
                StatusItemEntrada.PENDENTE_MANUTENCAO, StatusItemEntrada.AGUARDANDO_PECA));
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listarPorIds(List<UUID> ids) {
        return osRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public Page<OrdemServico> buscar(String busca, List<UUID> clienteIdsMatched,
                                      LocalDateTime dataInicial, LocalDateTime dataFinal, Pageable pageable) {
        return osRepository.buscar(busca, clienteIdsMatched, dataInicial, dataFinal, pageable);
    }

    /** Usado por outros módulos (Orçamento) pra resolver OS cujo número casa com uma busca textual. */
    @Transactional(readOnly = true)
    public List<UUID> buscarIdsPorNumero(String busca) {
        return osRepository.buscarIdsPorNumero(busca);
    }

    @Transactional
    public OrdemServico criar(UUID clienteId, UUID postoId, UUID tecnicoId, String solicitante) {
        return criar(clienteId, postoId, tecnicoId, solicitante, null, null);
    }

    @Transactional
    public OrdemServico criar(UUID clienteId, UUID postoId, UUID tecnicoId, String solicitante,
                               LocalDateTime dataAbertura, String observacoes) {
        OrdemServico.OrdemServicoBuilder builder = OrdemServico.builder()
                .numero(numeroGenerator.gerarNumero())
                .clienteId(clienteId)
                .postoId(postoId)
                .tecnicoId(tecnicoId)
                .solicitante(solicitante)
                .observacoes(observacoes);
        if (dataAbertura != null) {
            builder.dataAbertura(dataAbertura);
        }
        return osRepository.save(builder.build());
    }

    @Transactional
    public OrdemServico iniciarAndamento(UUID id) {
        OrdemServico os = buscarPorId(id);
        StatusOS statusAnterior = os.getStatus();
        os.iniciarAndamento();
        OrdemServico salva = osRepository.save(os);
        registrarTransicaoStatus(id, statusAnterior, salva.getStatus(), null);
        return salva;
    }

    @Transactional
    public OrdemServico confirmarEntrega(UUID id, String nomeRecebedor) {
        OrdemServico os = buscarPorId(id);
        StatusOS statusAnterior = os.getStatus();
        os.confirmarEntrega(nomeRecebedor);
        OrdemServico salva = osRepository.save(os);
        registrarTransicaoStatus(id, statusAnterior, salva.getStatus(), null);
        return salva;
    }

    @Transactional
    public OrdemServico cancelar(UUID id, String motivo) {
        OrdemServico os = buscarPorId(id);
        StatusOS statusAnterior = os.getStatus();
        os.cancelar(motivo);
        OrdemServico salva = osRepository.save(os);
        registrarTransicaoStatus(id, statusAnterior, salva.getStatus(), motivo);
        return salva;
    }

    /** Usado pela reordenação manual da fila de manutenção — as OS já vêm carregadas/mutadas pelo application service. */
    @Transactional
    public void salvarTodas(List<OrdemServico> ordens) {
        ordens.forEach(osRepository::save);
    }

    private void registrarTransicaoStatus(UUID ordemServicoId, StatusOS statusAnterior, StatusOS statusNovo, String motivo) {
        statusHistoricoRepository.save(OrdemServicoStatusHistorico.builder()
                .ordemServicoId(ordemServicoId)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .motivo(motivo != null && !motivo.isBlank() ? motivo.trim() : null)
                .build());
    }

    // ===== MOVIMENTAÇÃO DE ITENS ENTRE OS =====

    @Transactional
    public void moverItem(UUID itemId, UUID novaOsId) {
        buscarPorId(novaOsId); // valida que a OS destino existe
        ItemEntrada item = itemEntradaRepository.findById(itemId)
                .orElseThrow(() -> new DomainException("Item de entrada não encontrado: " + itemId));
        item.moverParaOS(novaOsId);
        itemEntradaRepository.save(item);
    }

    /**
     * Cria uma OS nova e move os itens escolhidos pra ela — atalho para o
     * caso "cliente aprovou só parte dos itens" ou "esse item já pode ser
     * entregue enquanto os outros esperam peça".
     */
    @Transactional
    public OrdemServico dividir(UUID osOrigemId, List<UUID> itemIds, String solicitante) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new DomainException("Selecione ao menos um item para dividir a OS");
        }
        OrdemServico osOrigem = buscarPorId(osOrigemId);
        OrdemServico novaOS = criar(osOrigem.getClienteId(), osOrigem.getPostoId(),
                osOrigem.getTecnicoId(), solicitante);
        itemIds.forEach(itemId -> moverItem(itemId, novaOS.getId()));
        return novaOS;
    }

    /**
     * Move todos os itens das OS de origem para a OS destino e cancela as
     * origens que ficarem vazias.
     */
    @Transactional
    public OrdemServico unir(UUID osDestinoId, List<UUID> osOrigemIds) {
        OrdemServico destino = buscarPorId(osDestinoId);
        for (UUID origemId : osOrigemIds) {
            if (origemId.equals(osDestinoId)) continue;

            List<ItemEntrada> itens = itemEntradaRepository.findByOsId(origemId);
            itens.forEach(item -> moverItem(item.getId(), osDestinoId));

            OrdemServico origem = buscarPorId(origemId);
            if (!origem.isEncerrada()) {
                cancelar(origemId, "Itens unidos à OS " + destino.getNumero());
            }
        }
        return destino;
    }
}
