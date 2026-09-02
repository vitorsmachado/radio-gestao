package com.radiocom.cliente.application.dto;

import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteCreateDTO {

    /** Se não informado, é inferido a partir do tamanho do documento (11 = CPF, 14 = CNPJ). */
    private TipoPessoa tipo;

    @NotBlank(message = "Documento é obrigatório")
    @Pattern(regexp = "^\\d{11}$|^\\d{14}$", message = "Documento inválido")
    private String documento;

    @NotBlank(message = "Nome/Razão Social é obrigatório")
    @Size(max = 255)
    private String nomeRazaoSocial;

    @Size(max = 255)
    private String nomeFantasia;

    @Size(max = 20)
    private String inscricaoEstadual;

    @Valid
    private EnderecoDTO endereco;

    @Valid
    private List<ContatoDTO> contatos;

    @Valid
    private List<PostoDTO> postos;
}
