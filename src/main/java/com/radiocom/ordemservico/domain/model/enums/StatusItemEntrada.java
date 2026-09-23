package com.radiocom.ordemservico.domain.model.enums;

/**
 * Ciclo de vida de um item de entrada (equipamento/acessório trazido pelo
 * cliente), independente do status da OS ou do orçamento em que ele está
 * atualmente agrupado.
 */
public enum StatusItemEntrada {
    PENDENTE_AVALIACAO("Pendente de avaliação"),
    EM_AVALIACAO("Em avaliação"),
    AVALIADO("Avaliado"),
    PENDENTE_AUTORIZACAO("Pendente de autorização"),
    AUTORIZADO("Autorizado"),
    NAO_AUTORIZADO("Não autorizado"),
    PENDENTE_MANUTENCAO("Pendente de manutenção"),
    AGUARDANDO_PECA("Aguardando peça"),
    EM_MANUTENCAO("Em manutenção"),
    MANUTENCAO_CONCLUIDA("Manutenção concluída"),
    AGUARDANDO_ENTREGA("Aguardando entrega"),
    ENTREGUE("Entregue");

    private final String label;

    StatusItemEntrada(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
