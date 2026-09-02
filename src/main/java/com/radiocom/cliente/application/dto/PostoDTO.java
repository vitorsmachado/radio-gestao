package com.radiocom.cliente.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostoDTO {

    private UUID id;

    @NotBlank
    @Size(max = 255)
    private String nome;

    @Valid
    private EnderecoDTO endereco;

    @Size(max = 255)
    private String responsavel;

    private boolean padrao;
}
