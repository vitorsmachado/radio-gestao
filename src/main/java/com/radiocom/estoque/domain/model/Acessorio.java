package com.radiocom.estoque.domain.model;

import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "acessorios", indexes = {
        @Index(name = "idx_acess_ns", columnList = "numero_serie"),
        @Index(name = "idx_acess_patrimonio", columnList = "patrimonio"),
        @Index(name = "idx_acess_tipo", columnList = "tipo_acessorio")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Acessorio extends ItemEstoque {

    @Enumerated(EnumType.STRING)
    @Column(name = "proprietario", length = 20)
    private ProprietarioEquipamento proprietario; // NOSSO | CLIENTE

    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(name = "numero_serie", length = 50)
    private String numeroSerie;

    @Column(name = "patrimonio", length = 50)
    private String patrimonio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acessorio", nullable = false, length = 30)
    private TipoAcessorio tipoAcessorio;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoEquipamento estado = EstadoEquipamento.DISPONIVEL;

    @Column(name = "garantia_fim")
    private LocalDate garantiaFim;

    @Override
    public boolean possuiNumeroSerie() {
        return numeroSerie != null && !numeroSerie.isBlank();
    }

    @Override
    public boolean possuiPatrimonio() {
        return patrimonio != null && !patrimonio.isBlank();
    }

    public boolean emGarantia() {
        return garantiaFim != null && !LocalDate.now().isAfter(garantiaFim);
    }

    public void enviarManutencao() {
        if (estado == EstadoEquipamento.MANUTENCAO) {
            throw new IllegalStateException("Acessório já está em manutenção");
        }
        this.estado = EstadoEquipamento.MANUTENCAO;
    }

    public void concluirManutencao() {
        this.estado = EstadoEquipamento.DISPONIVEL;
    }
}
