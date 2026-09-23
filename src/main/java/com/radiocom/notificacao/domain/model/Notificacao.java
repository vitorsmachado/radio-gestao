package com.radiocom.notificacao.domain.model;

import com.radiocom.notificacao.domain.model.enums.TipoNotificacao;
import com.radiocom.shared.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Aviso genérico pro admin agir — hoje só {@code GARANTIA_CONFLITO}, mas
 * pensado pra outros tipos futuros (ex: "orçamento pronto pra revisar") sem
 * precisar de estrutura nova.
 */
@Entity
@Table(name = "notificacoes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Notificacao extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNotificacao tipo;

    @NotBlank
    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "mensagem", length = 1000)
    private String mensagem;

    /** Rota do front pra onde a notificação leva ao ser clicada. */
    @Column(name = "link", length = 255)
    private String link;

    @NotNull
    @Column(name = "lida", nullable = false)
    @Builder.Default
    private boolean lida = false;

    public void marcarComoLida() {
        this.lida = true;
    }
}
