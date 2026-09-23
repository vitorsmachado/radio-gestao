package com.radiocom.ordemservico.domain.model.enums;

/** Conclusão do técnico ao avaliar um item de entrada. */
public enum ResultadoAvaliacao {
    AJUSTE("Ajuste"),
    ORCAMENTO("Precisa de orçamento"),
    SEM_DEFEITO("Sem defeito"),
    SEM_CONSERTO("Sem conserto");

    private final String label;

    ResultadoAvaliacao(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
