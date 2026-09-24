package com.radiocom.estoque.application.dto;

import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Usado ao registrar um item de entrada na OS: se o cliente já tem um
 * equipamento com esse número de série cadastrado, reaproveita (é o que
 * permite reconhecer o mesmo equipamento numa visita futura, para
 * garantia); senão, cadastra um novo vinculado a esse cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolverEquipamentoPorNSDTO {

    @NotBlank
    @Size(max = 50)
    private String numeroSerie;

    @NotNull
    private UUID clienteId;

    @NotNull
    private FaixaEquipamento faixa;

    @Size(max = 255)
    private String descricao;

    private UUID catalogoModeloId;

    @Size(max = 100)
    private String marca;

    @Size(max = 100)
    private String modelo;
}
