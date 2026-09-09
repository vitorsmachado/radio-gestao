package com.radiocom.estoque.domain.model.enums;

public enum TipoItem {
    EQUIPAMENTO("Equipamento"),
    ACESSORIO("Acessório"),
    PECA("Peça"),
    SERVICO("Serviço");

    private final String label;

    TipoItem(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
