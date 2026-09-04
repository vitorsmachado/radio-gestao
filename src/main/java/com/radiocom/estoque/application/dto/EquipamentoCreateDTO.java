package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipamentoCreateDTO {

    @NotNull
    private ProprietarioEquipamento proprietario;

    @NotBlank
    @Size(max = 50)
    private String numeroSerie;

    @Size(max = 50)
    private String patrimonio; // Obrigatório se proprietario = NOSSO

    private UUID clienteId; // Obrigatório se proprietario = CLIENTE

    @NotNull
    private FaixaEquipamento faixa;

    @Size(max = 255)
    private String descricao;

    private UUID catalogoModeloId; // Se ausente, resolve/cria pelo par marca+modelo

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    private LocalDate garantiaFim;

    private Map<String, String> especificacoes;
}
