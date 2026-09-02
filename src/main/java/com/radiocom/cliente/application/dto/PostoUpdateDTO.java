package com.radiocom.cliente.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Atualização parcial de posto — apenas campos não-nulos são aplicados.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostoUpdateDTO {

    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String nome;

    @Size(max = 255, message = "Responsável deve ter no máximo 255 caracteres")
    private String responsavel;

    @Valid
    private EnderecoDTO endereco;

    /** Quando true, define este posto como padrão e remove o padrão dos demais */
    private Boolean padrao;
}
