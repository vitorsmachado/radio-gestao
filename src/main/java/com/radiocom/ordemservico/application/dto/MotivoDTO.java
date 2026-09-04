package com.radiocom.ordemservico.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Usado tanto para não autorizar um item quanto para cancelar uma OS. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoDTO {

    @NotBlank
    @Size(max = 500)
    private String motivo;
}
