package com.radiocom.estoque.domain.model;

import com.radiocom.estoque.domain.model.enums.EstadoEquipamento;
import com.radiocom.estoque.domain.model.enums.FaixaEquipamento;
import com.radiocom.estoque.domain.model.enums.ProprietarioEquipamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "equipamentos", indexes = {
        @Index(name = "idx_equip_ns", columnList = "numero_serie"),
        @Index(name = "idx_equip_proprietario", columnList = "proprietario"),
        @Index(name = "idx_equip_faixa", columnList = "faixa"),
        @Index(name = "idx_equip_estado", columnList = "estado")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Equipamento extends ItemEstoque {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "proprietario", nullable = false, length = 20)
    private ProprietarioEquipamento proprietario; // NOSSO ou CLIENTE

    @NotBlank
    @Column(name = "numero_serie", nullable = false, length = 50)
    private String numeroSerie;

    @Column(name = "patrimonio", length = 50)
    private String patrimonio; // Obrigatório apenas se NOSSO

    @Column(name = "cliente_id")
    private UUID clienteId; // Se proprietario = CLIENTE

    @Column(name = "garantia_fim")
    private LocalDate garantiaFim;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "faixa", nullable = false, length = 20)
    private FaixaEquipamento faixa; // UHF, VHF, DUAL BAND

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoEquipamento estado = EstadoEquipamento.DISPONIVEL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "equipamento_especificacoes",
            joinColumns = @JoinColumn(name = "equipamento_id"))
    @MapKeyColumn(name = "chave", length = 50)
    @Column(name = "valor", length = 255)
    @Builder.Default
    private Map<String, String> especificacoes = new HashMap<>();

    @Override
    public boolean possuiNumeroSerie() {
        return true;
    }

    @Override
    public boolean possuiPatrimonio() {
        return patrimonio != null && !patrimonio.isBlank();
    }

    @PrePersist
    @PreUpdate
    public void validarPatrimonio() {
        if (proprietario == ProprietarioEquipamento.NOSSO &&
                (patrimonio == null || patrimonio.isBlank())) {
            throw new IllegalStateException("Equipamento NOSSO deve ter patrimônio");
        }
    }

    // ===== MÉTODOS DE ESTADO =====

    public void enviarManutencao() {
        if (estado == EstadoEquipamento.MANUTENCAO) {
            throw new IllegalStateException("Equipamento já está em manutenção");
        }
        this.estado = EstadoEquipamento.MANUTENCAO;
    }

    public void concluirManutencao() {
        if (estado != EstadoEquipamento.MANUTENCAO) {
            throw new IllegalStateException("Equipamento não está em manutenção. Estado atual: " + estado);
        }
        this.estado = EstadoEquipamento.DISPONIVEL;
    }

    public void marcarDescartado() {
        this.estado = EstadoEquipamento.DESCARTADO;
    }

    public boolean emGarantia() {
        return garantiaFim != null && !LocalDate.now().isAfter(garantiaFim);
    }
}
