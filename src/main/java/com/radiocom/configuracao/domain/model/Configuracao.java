package com.radiocom.configuracao.domain.model;

import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Configurações gerais do sistema — linha única (sempre existe uma, criada
 * pela migration). Pensado pra crescer conforme surgem outros valores
 * configuráveis, sem precisar de uma tela nova pra cada um.
 */
@Entity
@Table(name = "configuracoes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Configuracao extends BaseEntity {

    @NotNull
    @Column(name = "valor_mao_de_obra_padrao", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMaoDeObraPadrao;
}
