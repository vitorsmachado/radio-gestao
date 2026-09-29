package com.radiocom.configuracao.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarConfiguracaoDTO {

    @NotNull
    @PositiveOrZero
    private BigDecimal valorMaoDeObraPadrao;

    @NotNull
    @Positive
    private Integer prazoGarantiaPecaDias;

    @NotNull
    @Positive
    private Integer prazoGarantiaEquipamentoDias;

    @NotNull
    @Positive
    private Integer prazoGarantiaAcessorioDias;

    @NotBlank
    @Size(max = 255)
    private String nomeEmpresa;

    @Size(max = 255)
    private String razaoSocialEmpresa;

    @NotBlank
    @Size(max = 14)
    private String documentoEmpresa;

    @Size(max = 20)
    private String inscricaoEstadualEmpresa;

    @Size(max = 500)
    private String enderecoEmpresa;

    @Size(max = 100)
    private String bairroEmpresa;

    @Size(max = 100)
    private String cidadeEmpresa;

    @Size(max = 20)
    private String telefoneEmpresa;

    @Email
    @Size(max = 255)
    private String emailEmpresa;
}
