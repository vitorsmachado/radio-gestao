package com.radiocom.cliente.domain.model;

import com.radiocom.cliente.domain.model.enums.TipoContato;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Objects;

@Entity
@Table(name = "contatos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Contato extends BaseEntity {

    @NotBlank
    @Column(name = "nome", nullable = false)
    private String nome;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoContato tipo;

    @Column(name = "telefone", length = 11)
    private String telefone;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "cargo")
    private String cargo;

    @NotNull
    @Column(name = "principal", nullable = false)
    @Builder.Default
    private boolean principal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    private Cliente cliente;

    /**
     * equals/hashCode baseado no id quando persistido, ou em identidade de objeto
     * quando transiente (id == null). Garante que dois Contatos não persistidos
     * sejam tratados como distintos num Set, sem perder a corretude pós-persist.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contato other)) return false;
        if (getId() == null || other.getId() == null) return false;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? Objects.hash(getId()) : System.identityHashCode(this);
    }
}
