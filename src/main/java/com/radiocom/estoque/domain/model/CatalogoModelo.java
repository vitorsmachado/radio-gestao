package com.radiocom.estoque.domain.model;

import com.radiocom.estoque.domain.model.enums.StatusItem;
import com.radiocom.estoque.domain.model.enums.TipoAcessorio;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "catalogo_modelos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_catalogo_tipo_marca_modelo",
                columnNames = {"tipo_item", "marca", "modelo"})
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CatalogoModelo extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", nullable = false, length = 20)
    private TipoItem tipoItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acessorio", length = 30)
    private TipoAcessorio tipoAcessorio;

    @Column(name = "referencia", length = 50, unique = true)
    private String referencia;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String marca;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(length = 255)
    private String descricao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusItem status = StatusItem.ATIVO;

    @Column(name = "controle_por_serie", nullable = false)
    @Builder.Default
    private boolean controlePorSerie = false;

    /** true = itens deste modelo possuem número de patrimônio (independente de NS) */
    @Column(name = "possui_patrimonio", nullable = false)
    @Builder.Default
    private boolean possuiPatrimonio = false;
}
