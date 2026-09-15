package com.radiocom.cliente.domain.model;

import com.radiocom.cliente.domain.model.enums.StatusCliente;
import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.shared.model.BaseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "clientes", indexes = {
        @Index(name = "idx_cliente_documento", columnList = "documento", unique = true),
        @Index(name = "idx_cliente_status", columnList = "status"),
        @Index(name = "idx_cliente_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true, exclude = {"contatos", "postos"})
public class Cliente extends BaseEntity {

    /** Número interno do cliente (não é o documento) — atribuído na criação, editável depois. */
    @NotNull
    @Column(name = "numero_identificacao", nullable = false, unique = true)
    private Integer numeroIdentificacao;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPessoa tipo;

    @NotBlank
    @Pattern(regexp = "^\\d{11}$|^\\d{14}$",
            message = "Documento deve ter 11 (CPF) ou 14 (CNPJ) dígitos numéricos")
    @Column(name = "documento", nullable = false, unique = true, length = 14)
    @EqualsAndHashCode.Include
    private String documento;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome_razao_social", nullable = false)
    private String nomeRazaoSocial;

    @Size(max = 255)
    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Size(max = 20)
    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "numero", column = @Column(name = "end_numero", length = 20))
    })
    private Endereco endereco;

    // LinkedHashSet: resolve MultipleBagFetchException (Hibernate não suporta JOIN FETCH
    // simultâneo em dois List/bag). LinkedHashSet preserva ordem de inserção, e o
    // @OrderBy continua sendo aplicado pelo Hibernate nas queries.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @OrderBy("principal desc, nome asc")
    @Builder.Default
    private Set<Contato> contatos = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @Builder.Default
    private Set<Posto> postos = new LinkedHashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StatusCliente status = StatusCliente.ATIVO;

    // ===== Métodos de domínio =====

    public void adicionarPosto(Posto posto) {
        posto.setCliente(this);
        this.postos.add(posto);
    }

    public void removerPosto(Posto posto) {
        this.postos.remove(posto);
    }

    public void removerPostoPorId(UUID postoId) {
        Posto posto = this.postos.stream()
                .filter(p -> p.getId().equals(postoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Posto não encontrado: " + postoId));
        removerPosto(posto);
    }

    public void adicionarContato(Contato contato) {
        if (contato.isPrincipal() && temContatoPrincipal()) {
            throw new IllegalStateException("Já existe um contato principal");
        }
        contato.setCliente(this);
        this.contatos.add(contato);
    }

    public void removerContato(Contato contato) {
        this.contatos.remove(contato);
        contato.setCliente(null);
    }

    public void removerContatoPorId(UUID contatoId) {
        Contato contato = this.contatos.stream()
                .filter(c -> c.getId().equals(contatoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Contato não encontrado: " + contatoId));
        removerContato(contato);
    }

    public void atualizarContatoPrincipal(UUID contatoId) {
        Contato novoPrincipal = this.contatos.stream()
                .filter(c -> c.getId().equals(contatoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Contato não encontrado: " + contatoId));

        this.contatos.forEach(c -> c.setPrincipal(false));
        novoPrincipal.setPrincipal(true);
    }

    private boolean temContatoPrincipal() {
        return this.contatos.stream().anyMatch(Contato::isPrincipal);
    }

    // ===== STATUS =====

    public void inativar() {
        this.status = StatusCliente.INATIVO;
    }

    public void bloquear() {
        this.status = StatusCliente.BLOQUEADO;
    }

    public void ativar() {
        this.status = StatusCliente.ATIVO;
    }

    public boolean isAtivo() {
        return this.status == StatusCliente.ATIVO;
    }

    // ===== TIPO =====

    public boolean isPessoaFisica() {
        return this.tipo == TipoPessoa.PESSOA_FISICA;
    }

    public boolean isPessoaJuridica() {
        return this.tipo == TipoPessoa.PESSOA_JURIDICA;
    }
}
