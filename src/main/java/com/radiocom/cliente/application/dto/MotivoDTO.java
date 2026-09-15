package com.radiocom.cliente.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Motivo opcional de uma transição de status (ativar/inativar/bloquear). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MotivoDTO {

    @Size(max = 500)
    private String motivo;
}
