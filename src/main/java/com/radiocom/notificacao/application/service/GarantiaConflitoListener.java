package com.radiocom.notificacao.application.service;

import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
import com.radiocom.notificacao.domain.service.NotificacaoDomainService;
import com.radiocom.ordemservico.domain.event.GarantiaConflitoEvent;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reage a um item avaliado com defeito novo (não coberto) num equipamento que
 * ainda tem garantia ativa de outra peça — cria a notificação pro admin
 * decidir se cobre como garantia mesmo assim ou manda pra cobrança normal.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GarantiaConflitoListener {

    private final NotificacaoDomainService notificacaoDomainService;
    private final OrdemServicoDomainService osDomainService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onGarantiaConflito(GarantiaConflitoEvent event) {
        try {
            OrdemServico os = osDomainService.buscarPorId(event.getOsId());
            notificacaoDomainService.criar(
                    TipoNotificacao.GARANTIA_CONFLITO,
                    "Conflito de garantia na OS " + os.getNumero(),
                    "O equipamento tem garantia ativa de outra peça, mas foi avaliado um defeito novo. "
                            + "Decida se cobre como garantia mesmo assim ou envia pra cobrança normal.",
                    "/manutencao/" + event.getOsId());
        } catch (Exception e) {
            log.warn("Não foi possível criar notificação de conflito de garantia pro item {}: {}",
                    event.getItemEntradaId(), e.getMessage());
        }
    }
}
