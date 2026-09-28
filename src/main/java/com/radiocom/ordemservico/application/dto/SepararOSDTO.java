package com.radiocom.ordemservico.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SepararOSDTO {

    @NotEmpty
    @Valid
    private List<GrupoItensDTO> grupos;

    @Size(max = 100)
    private String solicitante;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrupoItensDTO {

        @NotEmpty
        private List<UUID> itemIds;
    }
}
