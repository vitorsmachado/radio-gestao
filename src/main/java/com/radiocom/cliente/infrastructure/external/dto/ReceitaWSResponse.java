package com.radiocom.cliente.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Campos relevantes da resposta da ReceitaWS (https://receitaws.com.br) — o resto é ignorado. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceitaWSResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("fantasia")
    private String fantasia;

    @JsonProperty("logradouro")
    private String logradouro;

    @JsonProperty("numero")
    private String numero;

    @JsonProperty("complemento")
    private String complemento;

    @JsonProperty("bairro")
    private String bairro;

    @JsonProperty("municipio")
    private String municipio;

    @JsonProperty("uf")
    private String uf;

    @JsonProperty("cep")
    private String cep;
}
