package com.radiocom.estoque.unit.domain.model;

import com.radiocom.estoque.domain.model.Acessorio;
import com.radiocom.estoque.domain.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Acessorio - Testes de Transição de Estado e Regras de Negócio")
class AcessorioTest {

    private Acessorio acessorio;

    @BeforeEach
    void setUp() {
        acessorio = Acessorio.builder()
                .codigo("AC-001")
                .descricao("Bateria")
                .tipo(TipoItem.ACESSORIO)
                .tipoAcessorio(TipoAcessorio.BATERIA)
                .estado(EstadoEquipamento.DISPONIVEL)
                .build();
    }

    @Test
    @DisplayName("enviarManutencao deve mudar estado para MANUTENCAO")
    void enviarManutencao_deveMudarParaManutencao() {
        acessorio.enviarManutencao();
        assertThat(acessorio.getEstado()).isEqualTo(EstadoEquipamento.MANUTENCAO);
    }

    @Test
    @DisplayName("enviarManutencao deve lançar exceção quando já em manutenção")
    void enviarManutencao_deveLancarExcecaoQuandoJaEmManutencao() {
        acessorio.setEstado(EstadoEquipamento.MANUTENCAO);
        assertThatThrownBy(acessorio::enviarManutencao)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está em manutenção");
    }

    @Test
    @DisplayName("concluirManutencao deve retornar estado para DISPONIVEL")
    void concluirManutencao_deveRetornarParaDisponivel() {
        acessorio.setEstado(EstadoEquipamento.MANUTENCAO);
        acessorio.concluirManutencao();
        assertThat(acessorio.getEstado()).isEqualTo(EstadoEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("possuiNumeroSerie deve retornar false quando não preenchido")
    void possuiNumeroSerie_deveRetornarFalseQuandoNaoPreenchido() {
        assertThat(acessorio.possuiNumeroSerie()).isFalse();
    }

    @Test
    @DisplayName("possuiNumeroSerie deve retornar true quando preenchido")
    void possuiNumeroSerie_deveRetornarTrueQuandoPreenchido() {
        acessorio.setNumeroSerie("NS-100");
        assertThat(acessorio.possuiNumeroSerie()).isTrue();
    }

    @Test
    @DisplayName("possuiPatrimonio deve retornar true quando preenchido")
    void possuiPatrimonio_deveRetornarTrueQuandoPreenchido() {
        acessorio.setPatrimonio("PAT-200");
        assertThat(acessorio.possuiPatrimonio()).isTrue();
    }
}
