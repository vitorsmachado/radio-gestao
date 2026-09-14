package com.radiocom.estoque.domain.model.enums;

/**
 * Filtro de criticidade de estoque para peças. EM_FALTA e ESTOQUE_BAIXO são
 * categorias mutuamente exclusivas; CRITICO é a união das duas (qualquer
 * peça que precise de atenção).
 */
public enum CriticidadeEstoque {
    EM_FALTA,
    ESTOQUE_BAIXO,
    CRITICO
}
