package com.radiocom.cliente.domain.model;

import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "postos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Posto extends BaseEntity {

    @NotBlank
    @Column(name = "nome", nullable = false)
    private String nome;

    @Valid
    @Embedded
    private Endereco endereco;

    @Column(name = "responsavel")
    private String responsavel;

    @Column(name = "padrao", nullable = false)
    @Builder.Default
    private boolean padrao = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @ToString.Exclude
    private Cliente cliente;

    /**
     * equals/hashCode baseado no id quando persistido, ou em identidade de objeto
     * quando transiente (id == null). Garante que dois Postos não persistidos
     * sejam tratados como distintos num Set, sem perder a corretude pós-persist.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Posto other)) return false;
        if (getId() == null || other.getId() == null) return false;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : System.identityHashCode(this);
    }
}
