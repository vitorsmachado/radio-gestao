package com.radiocom.ordemservico.domain.model.enums;

/**
 * Status agregado da OS — o detalhe fino do progresso mora em cada
 * ItemEntrada, não aqui.
 */
public enum StatusOS {
    ABERTA("Aberta"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String label;

    StatusOS(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
