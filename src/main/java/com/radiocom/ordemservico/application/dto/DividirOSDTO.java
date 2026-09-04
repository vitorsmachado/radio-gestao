package com.radiocom.ordemservico.application.dto;

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
public class DividirOSDTO {

    @NotEmpty
    private List<UUID> itemIds;

    @Size(max = 100)
    private String solicitante;
}
