package com.radiocom.estoque.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Peça de reposição usada em consertos — controlada por quantidade em
 * estoque, não por número de série/patrimônio individual.
 */
@Entity
@Table(name = "pecas")
@SuperBuilder
@NoArgsConstructor
public class Peca extends ItemEstoque {

    @Override
    public boolean possuiNumeroSerie() {
        return false;
    }

    @Override
    public boolean possuiPatrimonio() {
        return false;
    }
}
