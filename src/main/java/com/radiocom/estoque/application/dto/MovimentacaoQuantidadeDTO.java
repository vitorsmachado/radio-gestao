package com.radiocom.estoque.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoQuantidadeDTO {

    @NotNull
    @Positive
    private Integer quantidade;

    @Size(max = 500)
    private String motivo;
}
