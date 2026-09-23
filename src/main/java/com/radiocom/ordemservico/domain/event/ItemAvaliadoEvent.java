package com.radiocom.ordemservico.domain.event;

import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Publicado quando a avaliação técnica de um item é salva — permite que o
 * módulo de Orçamento reaja (agrupando o item num orçamento automaticamente)
 * sem que Ordem de Serviço precise conhecê-lo.
 */
@Getter
public class ItemAvaliadoEvent extends ApplicationEvent {

    private final UUID itemEntradaId;
    private final UUID osId;
    private final ResultadoAvaliacao resultado;

    public ItemAvaliadoEvent(Object source, UUID itemEntradaId, UUID osId, ResultadoAvaliacao resultado) {
        super(source);
        this.itemEntradaId = itemEntradaId;
        this.osId = osId;
        this.resultado = resultado;
    }
}
