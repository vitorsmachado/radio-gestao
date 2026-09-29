package com.radiocom.configuracao.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracaoDTO {

    private BigDecimal valorMaoDeObraPadrao;
    private Integer prazoGarantiaPecaDias;
    private Integer prazoGarantiaEquipamentoDias;
    private Integer prazoGarantiaAcessorioDias;

    private String nomeEmpresa;
    private String razaoSocialEmpresa;
    private String documentoEmpresa;
    private String inscricaoEstadualEmpresa;
    private String enderecoEmpresa;
    private String bairroEmpresa;
    private String cidadeEmpresa;
    private String telefoneEmpresa;
    private String emailEmpresa;
}
