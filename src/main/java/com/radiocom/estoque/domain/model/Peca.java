package com.radiocom.estoque.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Peça de reposição usada em consertos — controlada por quantidade em
 * estoque, não por número de série/patrimônio individual.
 */
@Entity
@Table(name = "pecas")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Peca extends ItemEstoque {

    /** Modelos de equipamento com os quais esta peça é compatível (ex: bateria que serve no EP450 e no DEP450). */
    @ManyToMany
    @JoinTable(
            name = "peca_modelos_compativeis",
            joinColumns = @JoinColumn(name = "peca_id"),
            inverseJoinColumns = @JoinColumn(name = "catalogo_modelo_id"))
    @Builder.Default
    private Set<CatalogoModelo> modelosCompativeis = new LinkedHashSet<>();

    @Override
    public boolean possuiNumeroSerie() {
        return false;
    }

    @Override
    public boolean possuiPatrimonio() {
        return false;
    }

    public void vincularModeloCompativel(CatalogoModelo modelo) {
        modelosCompativeis.add(modelo);
    }

    public void desvincularModeloCompativel(UUID catalogoModeloId) {
        modelosCompativeis.removeIf(m -> m.getId().equals(catalogoModeloId));
    }
}
