package com.radiocom.ordemservico.sugestao.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SugestaoTextoDTO {

    private UUID id;
    private String valor;
    private Integer contagemUso;
}
