package com.radiocom.orcamento.domain.model.enums;

/**
 * Classificação agregada calculada a partir do status de cada
 * {@link com.radiocom.ordemservico.domain.model.ItemEntrada} agrupado no
 * orçamento — nunca é persistida, sempre derivada na hora da consulta, para
 * nunca dessincronizar da aprovação real (que é por item).
 */
public enum StatusAprovacaoOrcamento {
    PENDENTE,
    AUTORIZADO,
    NAO_AUTORIZADO,
    PARCIALMENTE_AUTORIZADO
}
