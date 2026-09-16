package com.radiocom.estoque.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipamentoUpdateDTO {

    @Size(max = 255)
    private String descricao;

    @Size(max = 100)
    private String codigoCliente;

    private LocalDate garantiaFim;

    private Map<String, String> especificacoes;

    @Size(max = 1000)
    private String observacoes;

    @Size(max = 50)
    private String localizacaoFisica;
}
