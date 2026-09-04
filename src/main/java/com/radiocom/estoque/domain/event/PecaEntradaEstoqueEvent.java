package com.radiocom.estoque.domain.event;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Publicado quando um item controlado por quantidade (Acessório ou Peça)
 * recebe entrada no estoque — permite que outros módulos (Ordem de Serviço)
 * reajam sem que o Estoque precise conhecê-los.
 */
@Getter
public class PecaEntradaEstoqueEvent extends ApplicationEvent {

    private final UUID itemId;
    private final TipoItem tipoItem;
    private final Integer quantidade;

    public PecaEntradaEstoqueEvent(Object source, UUID itemId, TipoItem tipoItem, Integer quantidade) {
        super(source);
        this.itemId = itemId;
        this.tipoItem = tipoItem;
        this.quantidade = quantidade;
    }
}
