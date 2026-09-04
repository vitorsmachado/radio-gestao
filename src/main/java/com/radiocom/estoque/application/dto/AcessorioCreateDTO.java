package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcessorioCreateDTO {

    @NotBlank
    @Size(max = 255)
    private String descricao;

    @NotNull
    private TipoAcessorio tipoAcessorio;

    private ProprietarioEquipamento proprietario;

    private UUID clienteId;

    @Size(max = 50)
    private String numeroSerie; // Se rastreado individualmente

    @Size(max = 50)
    private String patrimonio; // Se rastreado individualmente

    @PositiveOrZero
    private Integer quantidadeDisponivel; // Se controlado por quantidade

    @PositiveOrZero
    private Integer quantidadeMinima;

    private UUID catalogoModeloId; // Se ausente, resolve/cria pelo par marca+modelo

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;

    private LocalDate garantiaFim;
}
