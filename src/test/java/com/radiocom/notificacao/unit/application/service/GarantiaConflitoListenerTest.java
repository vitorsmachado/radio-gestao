package com.radiocom.notificacao.unit.application.service;

import com.radiocom.notificacao.application.service.GarantiaConflitoListener;
import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
import com.radiocom.notificacao.domain.service.NotificacaoDomainService;
import com.radiocom.ordemservico.domain.event.GarantiaConflitoEvent;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GarantiaConflitoListener - Testes Unitários")
class GarantiaConflitoListenerTest {

    @Mock private NotificacaoDomainService notificacaoDomainService;
    @Mock private OrdemServicoDomainService osDomainService;

    @InjectMocks
    private GarantiaConflitoListener listener;

    @Test
    @DisplayName("onGarantiaConflito deve criar notificação com link pra tela de manutenção")
    void onGarantiaConflito_deveCriarNotificacao() {
        UUID itemEntradaId = UUID.randomUUID();
        UUID osId = UUID.randomUUID();
        UUID itemEstoqueId = UUID.randomUUID();
        OrdemServico os = OrdemServico.builder().clienteId(UUID.randomUUID()).numero("OS-2026-0001").build();
        when(osDomainService.buscarPorId(osId)).thenReturn(os);

        listener.onGarantiaConflito(new GarantiaConflitoEvent(this, itemEntradaId, osId, itemEstoqueId));

        verify(notificacaoDomainService).criar(
                eq(TipoNotificacao.GARANTIA_CONFLITO), any(), any(), eq("/manutencao/" + osId));
    }

    @Test
    @DisplayName("onGarantiaConflito não deve propagar exceção")
    void onGarantiaConflito_naoDevePropagarExcecao() {
        UUID osId = UUID.randomUUID();
        when(osDomainService.buscarPorId(osId)).thenThrow(new DomainException("OS não encontrada"));

        listener.onGarantiaConflito(new GarantiaConflitoEvent(this, UUID.randomUUID(), osId, UUID.randomUUID()));
    }
}
