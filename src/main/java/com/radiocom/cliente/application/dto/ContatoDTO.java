package com.radiocom.cliente.application.dto;

import com.radiocom.cliente.domain.model.enums.TipoContato;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContatoDTO {

    private UUID id;

    @NotBlank
    @Size(max = 255)
    private String nome;

    @NotNull
    private TipoContato tipo;

    private String telefone;

    @Email
    private String email;

    @Size(max = 100)
    private String cargo;

    private boolean principal;
}
