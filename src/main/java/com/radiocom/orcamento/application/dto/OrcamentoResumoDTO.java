package com.radiocom.orcamento.application.dto;

import com.radiocom.orcamento.domain.model.enums.StatusAprovacaoOrcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoResumoDTO {

    private UUID id;
    private String numero;
    private UUID osId;
    private String osNumero;
    private UUID clienteId;
    private String clienteNome;
    private StatusOrcamento status;
    private StatusAprovacaoOrcamento statusAprovacao;
    private LocalDateTime dataEmissao;
    private LocalDate validade;
    private boolean expirado;
    private BigDecimal valorTotal;
    private int quantidadeItens;
    private String resumoItens;
}
