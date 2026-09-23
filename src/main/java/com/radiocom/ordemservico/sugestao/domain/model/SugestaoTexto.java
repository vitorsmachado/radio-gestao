package com.radiocom.ordemservico.sugestao.domain.model;

import com.radiocom.ordemservico.sugestao.domain.model.enums.CampoSugestao;
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
 * Um valor já digitado antes num campo de laudo técnico, reaproveitável como
 * sugestão de autocomplete. {@code contagemUso} cresce toda vez que o mesmo
 * texto é salvo de novo — usada pra ordenar as sugestões mais usadas primeiro.
 */
@Entity
@Table(name = "sugestoes_texto")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SugestaoTexto extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "campo", nullable = false, length = 30)
    private CampoSugestao campo;

    @NotBlank
    @Column(name = "valor", nullable = false, length = 1000)
    private String valor;

    @NotNull
    @Column(name = "contagem_uso", nullable = false)
    @Builder.Default
    private Integer contagemUso = 1;

    public void registrarUso() {
        this.contagemUso = this.contagemUso + 1;
    }
}
