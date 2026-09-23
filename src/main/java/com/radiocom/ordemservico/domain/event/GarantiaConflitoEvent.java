package com.radiocom.ordemservico.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Publicado quando um item é avaliado com defeito novo (não coberto por
 * garantia) mas o equipamento ainda tem cobertura ativa de outra peça —
 * o admin precisa decidir se honra como garantia mesmo assim ou cobra.
 */
@Getter
public class GarantiaConflitoEvent extends ApplicationEvent {

    private final UUID itemEntradaId;
    private final UUID osId;
    private final UUID itemEstoqueId;

    public GarantiaConflitoEvent(Object source, UUID itemEntradaId, UUID osId, UUID itemEstoqueId) {
        super(source);
        this.itemEntradaId = itemEntradaId;
        this.osId = osId;
        this.itemEstoqueId = itemEstoqueId;
    }
}
