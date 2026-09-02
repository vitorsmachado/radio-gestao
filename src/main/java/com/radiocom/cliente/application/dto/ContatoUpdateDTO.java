package com.radiocom.cliente.application.dto;

import com.radiocom.cliente.domain.model.enums.TipoContato;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Atualização parcial de contato — apenas campos não-nulos são aplicados.
 * "principal" fica de fora: use PATCH /{clienteId}/contatos/{contatoId}/principal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContatoUpdateDTO {

    @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
    private String nome;

    private TipoContato tipo;

    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter 10 ou 11 dígitos")
    private String telefone;

    @Email(message = "E-mail inválido")
    private String email;

    @Size(max = 100, message = "Cargo deve ter no máximo 100 caracteres")
    private String cargo;
}
