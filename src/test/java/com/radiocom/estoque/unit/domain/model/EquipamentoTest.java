package com.radiocom.estoque.unit.domain.model;

import com.radiocom.estoque.domain.model.Equipamento;
import com.radiocom.estoque.domain.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Equipamento - Testes de Transição de Estado e Regras de Negócio")
class EquipamentoTest {

    private Equipamento equipamento;

    @BeforeEach
    void setUp() {
        equipamento = Equipamento.builder()
                .codigo("EQ-001")
                .descricao("Rádio VHF")
                .tipo(TipoItem.EQUIPAMENTO)
                .proprietario(ProprietarioEquipamento.NOSSO)
                .faixa(FaixaEquipamento.VHF)
                .numeroSerie("NS-001")
                .patrimonio("PAT-001")
                .estado(EstadoEquipamento.DISPONIVEL)
                .build();
    }

    // ===== MANUTENÇÃO =====

    @Test
    @DisplayName("enviarManutencao deve mudar estado para MANUTENCAO")
    void enviarManutencao_deveMudarParaManutencao() {
        equipamento.enviarManutencao();
        assertThat(equipamento.getEstado()).isEqualTo(EstadoEquipamento.MANUTENCAO);
    }

    @Test
    @DisplayName("enviarManutencao deve lançar exceção quando já em manutenção")
    void enviarManutencao_deveLancarExcecaoQuandoJaEmManutencao() {
        equipamento.setEstado(EstadoEquipamento.MANUTENCAO);
        assertThatThrownBy(equipamento::enviarManutencao)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já está em manutenção");
    }

    @Test
    @DisplayName("concluirManutencao deve retornar estado para DISPONIVEL")
    void concluirManutencao_deveRetornarParaDisponivel() {
        equipamento.setEstado(EstadoEquipamento.MANUTENCAO);
        equipamento.concluirManutencao();
        assertThat(equipamento.getEstado()).isEqualTo(EstadoEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("concluirManutencao deve lançar exceção quando não está em manutenção")
    void concluirManutencao_deveLancarExcecaoQuandoNaoEmManutencao() {
        assertThatThrownBy(equipamento::concluirManutencao)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não está em manutenção");
    }

    @Test
    @DisplayName("marcarDescartado deve mudar estado para DESCARTADO")
    void marcarDescartado_deveMudarParaDescartado() {
        equipamento.marcarDescartado();
        assertThat(equipamento.getEstado()).isEqualTo(EstadoEquipamento.DESCARTADO);
    }

    // ===== VALIDAÇÃO DE PATRIMÔNIO =====

    @Test
    @DisplayName("validarPatrimonio deve lançar exceção quando NOSSO sem patrimônio")
    void validarPatrimonio_deveLancarExcecaoQuandoNossoSemPatrimonio() {
        equipamento.setPatrimonio(null);
        assertThatThrownBy(equipamento::validarPatrimonio)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deve ter patrimônio");
    }

    @Test
    @DisplayName("validarPatrimonio não deve lançar exceção quando CLIENTE sem patrimônio")
    void validarPatrimonio_naoDeveLancarQuandoClienteSemPatrimonio() {
        equipamento.setProprietario(ProprietarioEquipamento.CLIENTE);
        equipamento.setPatrimonio(null);
        equipamento.validarPatrimonio();
    }

    // ===== CONSULTAS =====

    @Test
    @DisplayName("possuiNumeroSerie deve sempre retornar true para Equipamento")
    void possuiNumeroSerie_sempreTrue() {
        assertThat(equipamento.possuiNumeroSerie()).isTrue();
    }

    @Test
    @DisplayName("possuiPatrimonio deve retornar true quando patrimônio preenchido")
    void possuiPatrimonio_deveRetornarTrueQuandoPreenchido() {
        assertThat(equipamento.possuiPatrimonio()).isTrue();
    }

    @Test
    @DisplayName("possuiPatrimonio deve retornar false quando patrimônio nulo")
    void possuiPatrimonio_deveRetornarFalseQuandoNulo() {
        equipamento.setPatrimonio(null);
        assertThat(equipamento.possuiPatrimonio()).isFalse();
    }

    @Test
    @DisplayName("emGarantia deve retornar true quando data futura")
    void emGarantia_deveRetornarTrueQuandoDataFutura() {
        equipamento.setGarantiaFim(LocalDate.now().plusDays(30));
        assertThat(equipamento.emGarantia()).isTrue();
    }

    @Test
    @DisplayName("emGarantia deve retornar false quando data nula")
    void emGarantia_deveRetornarFalseQuandoNulo() {
        assertThat(equipamento.emGarantia()).isFalse();
    }

    @Test
    @DisplayName("emGarantia deve retornar false quando data passada")
    void emGarantia_deveRetornarFalseQuandoDataPassada() {
        equipamento.setGarantiaFim(LocalDate.now().minusDays(1));
        assertThat(equipamento.emGarantia()).isFalse();
    }
}
