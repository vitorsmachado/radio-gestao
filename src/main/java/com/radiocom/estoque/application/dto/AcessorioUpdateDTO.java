package com.radiocom.estoque.application.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcessorioUpdateDTO {

    @Size(max = 255)
    private String descricao;

    @PositiveOrZero
    private Integer quantidadeMinima;

    private LocalDate garantiaFim;

    @Size(max = 1000)
    private String observacoes;

    @Size(max = 50)
    private String localizacaoFisica;
}
