package com.radiocom.ordemservico.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvaliarItemDTO {

    @Size(max = 1000)
    private String avaliacaoTecnica;

    private boolean semDefeito;
}
