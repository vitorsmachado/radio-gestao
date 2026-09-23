package com.radiocom.orcamento.application.service;

import com.radiocom.ordemservico.domain.event.ItemAvaliadoEvent;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reage à conclusão de uma avaliação técnica — o item entra automaticamente
 * no orçamento RASCUNHO da OS (criando um se ainda não existir). Itens
 * marcados SEM_DEFEITO não publicam o evento (ver ItemEntradaApplicationService).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrcamentoItemAvaliadoListener {

    private final OrcamentoDomainService orcamentoDomainService;
    private final OrdemServicoDomainService osDomainService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onItemAvaliado(ItemAvaliadoEvent event) {
        try {
            OrdemServico os = osDomainService.buscarPorId(event.getOsId());
            Orcamento orcamento = orcamentoDomainService.buscarOuCriarRascunho(event.getOsId(), os.getClienteId());
            orcamentoDomainService.adicionarItem(orcamento.getId(), event.getItemEntradaId());
        } catch (Exception e) {
            log.warn("Não foi possível agrupar o item {} num orçamento automático da OS {}: {}",
                    event.getItemEntradaId(), event.getOsId(), e.getMessage());
        }
    }
}
