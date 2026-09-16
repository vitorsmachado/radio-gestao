package com.radiocom.cliente.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Dados de um CNPJ consultados na ReceitaWS, prontos pra preencher o formulário de cliente. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaCnpjDTO {

    private String nomeRazaoSocial;
    private String nomeFantasia;
    private EnderecoDTO endereco;
}
