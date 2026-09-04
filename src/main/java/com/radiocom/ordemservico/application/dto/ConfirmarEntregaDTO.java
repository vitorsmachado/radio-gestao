package com.radiocom.ordemservico.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmarEntregaDTO {

    @NotBlank
    @Size(max = 100)
    private String nomeRecebedor;
}
