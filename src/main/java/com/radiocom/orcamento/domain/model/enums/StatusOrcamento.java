package com.radiocom.orcamento.domain.model.enums;

public enum StatusOrcamento {
    RASCUNHO("Rascunho"),
    ENVIADO("Enviado"),
    CANCELADO("Cancelado");

    private final String label;

    StatusOrcamento(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
