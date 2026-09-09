package com.radiocom.orcamento.domain.model.enums;

/**
 * Classificação agregada calculada a partir do status de cada
 * {@link com.radiocom.ordemservico.domain.model.ItemEntrada} agrupado no
 * orçamento — nunca é persistida, sempre derivada na hora da consulta, para
 * nunca dessincronizar da aprovação real (que é por item).
 */
public enum StatusAprovacaoOrcamento {
    PENDENTE("Pendente"),
    AUTORIZADO("Autorizado"),
    NAO_AUTORIZADO("Não autorizado"),
    PARCIALMENTE_AUTORIZADO("Parcialmente autorizado");

    private final String label;

    StatusAprovacaoOrcamento(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
