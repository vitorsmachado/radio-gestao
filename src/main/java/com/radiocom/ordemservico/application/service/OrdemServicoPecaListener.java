package com.radiocom.ordemservico.application.service;

import com.radiocom.estoque.domain.event.PecaEntradaEstoqueEvent;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reage à chegada de uma peça no estoque — itens de entrada que estavam
 * aguardando exatamente essa peça (e já têm quantidade suficiente) voltam
 * pra fila de manutenção automaticamente.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrdemServicoPecaListener {

    private final ItemEntradaDomainService itemEntradaDomainService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onPecaEntradaEstoque(PecaEntradaEstoqueEvent event) {
        try {
            itemEntradaDomainService.retomarItensAguardandoPeca(event.getItemId());
        } catch (Exception e) {
            log.warn("Não foi possível retomar itens aguardando a peça {}: {}",
                    event.getItemId(), e.getMessage());
        }
    }
}
