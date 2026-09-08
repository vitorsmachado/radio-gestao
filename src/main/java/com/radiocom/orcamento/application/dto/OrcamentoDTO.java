package com.radiocom.orcamento.application.dto;

import com.radiocom.ordemservico.application.dto.ItemEntradaDTO;
import com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoDTO {

    private UUID id;
    private String numero;
    private UUID osId;
    private UUID clienteId;
    private LocalDate validade;
    private String condicoesPagamento;
    private BigDecimal desconto;
    private StatusOrcamento status;
    private LocalDateTime dataEmissao;
    private String observacoes;
    private boolean expirado;
    private BigDecimal valorTotal;
    private StatusAprovacaoOrcamento statusAprovacao;
    private List<ItemEntradaDTO> itens;
}
