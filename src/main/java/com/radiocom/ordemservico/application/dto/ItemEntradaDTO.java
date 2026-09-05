package com.radiocom.ordemservico.application.dto;

import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.ordemservico.domain.model.enums.StatusItemEntrada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private TipoItem tipoItem;
    private String descricao;
    private String numeroSerie;
    private String patrimonio;
    private String marca;
    private String modelo;
    private String defeitoRelatado;
    private String avaliacaoTecnica;
    private boolean semDefeito;
    private boolean garantia;
    private StatusItemEntrada status;
    private String motivoNaoAutorizado;
    private List<ItemConsertoDTO> itensConserto;
    private BigDecimal valorTotalConserto;
}
