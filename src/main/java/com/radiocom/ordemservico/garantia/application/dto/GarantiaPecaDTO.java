package com.radiocom.ordemservico.garantia.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarantiaPecaDTO {

    private UUID id;
    private UUID pecaEstoqueId;
    private String descricaoPeca;
    private LocalDate dataInicio;
    private LocalDate dataFim;
}
