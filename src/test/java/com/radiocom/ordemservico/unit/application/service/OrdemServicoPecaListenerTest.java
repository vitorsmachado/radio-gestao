package com.radiocom.ordemservico.unit.application.service;

import com.radiocom.estoque.domain.event.PecaEntradaEstoqueEvent;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.application.service.OrdemServicoPecaListener;
import com.radiocom.ordemservico.domain.service.ItemEntradaDomainService;
import com.radiocom.shared.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoPecaListener - Testes Unitários")
class OrdemServicoPecaListenerTest {

    @Mock private ItemEntradaDomainService itemEntradaDomainService;

    @InjectMocks
    private OrdemServicoPecaListener listener;

    @Test
    @DisplayName("onPecaEntradaEstoque deve delegar para o domain service")
    void onPecaEntradaEstoque_deveDelegar() {
        UUID pecaId = UUID.randomUUID();
        PecaEntradaEstoqueEvent event = new PecaEntradaEstoqueEvent(this, pecaId, TipoItem.PECA, 5);

        listener.onPecaEntradaEstoque(event);

        verify(itemEntradaDomainService).retomarItensAguardandoPeca(pecaId);
    }

    @Test
    @DisplayName("onPecaEntradaEstoque não deve propagar exceção do domain service")
    void onPecaEntradaEstoque_naoDevePropagarExcecao() {
        UUID pecaId = UUID.randomUUID();
        PecaEntradaEstoqueEvent event = new PecaEntradaEstoqueEvent(this, pecaId, TipoItem.PECA, 5);
        doThrow(new DomainException("falha")).when(itemEntradaDomainService).retomarItensAguardandoPeca(pecaId);

        listener.onPecaEntradaEstoque(event); // não deve lançar
    }
}
