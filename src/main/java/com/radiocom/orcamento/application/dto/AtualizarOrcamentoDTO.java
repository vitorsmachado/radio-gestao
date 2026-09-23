package com.radiocom.orcamento.application.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarOrcamentoDTO {

    private LocalDate validade;

    private String condicoesPagamento;

    @PositiveOrZero
    private BigDecimal desconto;
}
