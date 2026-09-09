package com.radiocom.ordemservico.domain.model.enums;

public enum TipoItemConserto {
    PECA("Peça"),
    MAO_DE_OBRA("Mão de obra"),
    DESLOCAMENTO("Deslocamento");

    private final String label;

    TipoItemConserto(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
