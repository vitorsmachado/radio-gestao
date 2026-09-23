package com.radiocom.orcamento.unit.application.service;

import com.radiocom.ordemservico.domain.event.ItemAvaliadoEvent;
import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.service.OrdemServicoDomainService;
import com.radiocom.orcamento.application.service.OrcamentoItemAvaliadoListener;
import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.service.OrcamentoDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrcamentoItemAvaliadoListener - Testes Unitários")
class OrcamentoItemAvaliadoListenerTest {

    @Mock private OrcamentoDomainService orcamentoDomainService;
    @Mock private OrdemServicoDomainService osDomainService;

    @InjectMocks
    private OrcamentoItemAvaliadoListener listener;

    private UUID osId;
    private UUID clienteId;
    private UUID itemId;
    private UUID orcamentoId;
    private OrdemServico os;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        orcamentoId = UUID.randomUUID();
        os = OrdemServico.builder().clienteId(clienteId).build();
        ReflectionTestUtils.setField(os, "id", osId);
    }

    @Test
    @DisplayName("onItemAvaliado deve agrupar o item no orçamento RASCUNHO da OS")
    void onItemAvaliado_deveAgruparNoRascunho() {
        ItemAvaliadoEvent event = new ItemAvaliadoEvent(this, itemId, osId, ResultadoAvaliacao.ORCAMENTO);
        Orcamento orcamento = Orcamento.builder().osId(osId).clienteId(clienteId).build();
        ReflectionTestUtils.setField(orcamento, "id", orcamentoId);
        when(osDomainService.buscarPorId(osId)).thenReturn(os);
        when(orcamentoDomainService.buscarOuCriarRascunho(osId, clienteId)).thenReturn(orcamento);

        listener.onItemAvaliado(event);

        verify(orcamentoDomainService).adicionarItem(orcamentoId, itemId);
    }

    @Test
    @DisplayName("onItemAvaliado não deve propagar exceção")
    void onItemAvaliado_naoDevePropagarExcecao() {
        ItemAvaliadoEvent event = new ItemAvaliadoEvent(this, itemId, osId, ResultadoAvaliacao.ORCAMENTO);
        when(osDomainService.buscarPorId(osId)).thenThrow(new DomainException("OS não encontrada"));

        listener.onItemAvaliado(event); // não deve lançar

        verify(orcamentoDomainService, org.mockito.Mockito.never()).adicionarItem(any(), any());
    }
}
