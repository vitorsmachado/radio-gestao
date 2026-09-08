package com.radiocom.orcamento.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoCreateDTO {

    @NotNull
    private UUID osId;

    @NotNull
    private UUID clienteId;

    private LocalDate validade;

    private String condicoesPagamento;

    @PositiveOrZero
    private BigDecimal desconto;
}
