package com.radiocom.cliente.application.dto;

import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    private UUID id;

    @NotNull(message = "Tipo de pessoa é obrigatório")
    private TipoPessoa tipo;

    @NotBlank(message = "Documento é obrigatório")
    @Pattern(regexp = "^\\d{11}$|^\\d{14}$", message = "Documento inválido. Use 11 dígitos para CPF ou 14 para CNPJ")
    private String documento;

    @NotBlank(message = "Nome/Razão Social é obrigatório")
    @Size(max = 255)
    private String nomeRazaoSocial;

    @Size(max = 255)
    private String nomeFantasia;

    @Size(max = 20)
    private String inscricaoEstadual;

    private StatusCliente status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    private EnderecoDTO endereco;

    private List<ContatoDTO> contatos;
    private List<PostoDTO> postos;
}
