package com.radiocom.estoque.domain.model.enums;

public enum FaixaEquipamento {
    VHF("VHF"),
    UHF("UHF"),
    DUAL_BAND("Dual Band");

    private final String label;

    FaixaEquipamento(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
