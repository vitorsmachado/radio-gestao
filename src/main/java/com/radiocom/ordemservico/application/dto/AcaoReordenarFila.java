package com.radiocom.ordemservico.application.dto;

/** Ação de reordenação manual da fila de manutenção — sempre dentro do bloco atual da OS. */
public enum AcaoReordenarFila {
    SUBIR,
    DESCER,
    POSICAO
}
