package com.radiocom.ordemservico.application.dto;

import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.enums.ResultadoAvaliacao;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemEntradaDTO {

    private UUID id;
    private UUID osId;
    private UUID orcamentoId;
    private UUID itemEstoqueId;
    private UUID catalogoModeloId;
    /** Resolvido à parte (não vem do mapper puro) — preço de referência do modelo de catálogo, quando houver. */
    private BigDecimal catalogoValorReferencia;
    private TipoItem tipoItem;
    private String descricao;
    private String numeroSerie;
    private String patrimonio;
    private String codigoCliente;
    private Integer quantidade;
    private String marca;
    private String modelo;
    private FaixaEquipamento faixa;
    private String defeitoRelatado;
    private String avaliacaoTecnica;
    private boolean semDefeito;
    private boolean garantia;
    private StatusItemEntrada status;
    private String motivoNaoAutorizado;
    private ResultadoAvaliacao resultadoAvaliacao;
    private String detalheAjuste;
    private String defeitoEncontrado;
    private String causaDefeito;
    private String solucaoRecomendada;
    private String observacoesTecnicas;
    private LocalDateTime confirmadoAguardandoPecaEm;
    private List<ItemConsertoDTO> itensConserto;
    private BigDecimal valorTotalConserto;
}
