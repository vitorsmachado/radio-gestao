package com.radiocom.ordemservico.domain.model.enums;

/**
 * Ciclo de vida de um item de entrada (equipamento/acessório trazido pelo
 * cliente), independente do status da OS ou do orçamento em que ele está
 * atualmente agrupado.
 */
public enum StatusItemEntrada {
    PENDENTE_AVALIACAO,
    AVALIADO,
    PENDENTE_AUTORIZACAO,
    AUTORIZADO,
    NAO_AUTORIZADO,
    PENDENTE_MANUTENCAO,
    AGUARDANDO_PECA,
    EM_MANUTENCAO,
    MANUTENCAO_CONCLUIDA,
    AGUARDANDO_ENTREGA,
    ENTREGUE
}
