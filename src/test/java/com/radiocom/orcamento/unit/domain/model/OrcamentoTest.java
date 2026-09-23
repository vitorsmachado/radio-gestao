package com.radiocom.orcamento.unit.domain.model;

import com.radiocom.orcamento.domain.model.Orcamento;
import com.radiocom.orcamento.domain.model.enums.StatusOrcamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Orcamento - Testes de Domínio")
class OrcamentoTest {

    private Orcamento orcamento;

    @BeforeEach
    void setUp() {
        orcamento = Orcamento.builder()
                .numero("ORC-2026-0001")
                .osId(UUID.randomUUID())
                .clienteId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("novo orçamento deve nascer RASCUNHO")
    void novoOrcamento_deveNascerRascunho() {
        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.RASCUNHO);
    }

    @Test
    @DisplayName("enviar deve mudar status para ENVIADO")
    void enviar_deveMudarStatus() {
        orcamento.enviar();

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.ENVIADO);
    }

    @Test
    @DisplayName("enviar deve lançar exceção quando não está RASCUNHO")
    void enviar_deveLancarExcecaoQuandoNaoRascunho() {
        orcamento.enviar();

        assertThatThrownBy(orcamento::enviar)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("cancelar deve mudar status e registrar motivo nas observações")
    void cancelar_deveMudarStatusERegistrarMotivo() {
        orcamento.cancelar("Cliente desistiu");

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
        assertThat(orcamento.getObservacoes()).contains("Cliente desistiu");
    }

    @Test
    @DisplayName("cancelar deve funcionar a partir de ENVIADO")
    void cancelar_deveFuncionarAPartirDeEnviado() {
        orcamento.enviar();

        orcamento.cancelar("Cliente desistiu depois de enviado");

        assertThat(orcamento.getStatus()).isEqualTo(StatusOrcamento.CANCELADO);
    }

    @Test
    @DisplayName("cancelar deve lançar exceção quando já cancelado")
    void cancelar_deveLancarExcecaoQuandoJaCancelado() {
        orcamento.cancelar("motivo");

        assertThatThrownBy(() -> orcamento.cancelar("outro motivo"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("isExpirado deve retornar false quando não há validade")
    void isExpirado_deveRetornarFalseQuandoSemValidade() {
        orcamento.enviar();

        assertThat(orcamento.isExpirado()).isFalse();
    }

    @Test
    @DisplayName("isExpirado deve retornar false quando ainda em RASCUNHO, mesmo com validade vencida")
    void isExpirado_deveRetornarFalseQuandoRascunho() {
        orcamento.setValidade(LocalDate.now().minusDays(1));

        assertThat(orcamento.isExpirado()).isFalse();
    }

    @Test
    @DisplayName("isExpirado deve retornar true quando enviado e validade já passou")
    void isExpirado_deveRetornarTrueQuandoEnviadoEVencido() {
        orcamento.setValidade(LocalDate.now().minusDays(1));
        orcamento.enviar();

        assertThat(orcamento.isExpirado()).isTrue();
    }

    @Test
    @DisplayName("isExpirado deve retornar false quando enviado e validade ainda não passou")
    void isExpirado_deveRetornarFalseQuandoEnviadoENaoVencido() {
        orcamento.setValidade(LocalDate.now().plusDays(1));
        orcamento.enviar();

        assertThat(orcamento.isExpirado()).isFalse();
    }

    @Test
    @DisplayName("atualizarCondicoes deve aplicar só os campos informados")
    void atualizarCondicoes_deveAplicarSoOsCamposInformados() {
        orcamento.setCondicoesPagamento("À vista");

        orcamento.atualizarCondicoes(LocalDate.now().plusDays(30), null, new BigDecimal("10.00"));

        assertThat(orcamento.getValidade()).isEqualTo(LocalDate.now().plusDays(30));
        assertThat(orcamento.getCondicoesPagamento()).isEqualTo("À vista");
        assertThat(orcamento.getDesconto()).isEqualByComparingTo("10.00");
    }

    @Test
    @DisplayName("atualizarCondicoes deve lançar exceção quando não está RASCUNHO")
    void atualizarCondicoes_deveLancarExcecaoQuandoNaoRascunho() {
        orcamento.enviar();

        assertThatThrownBy(() -> orcamento.atualizarCondicoes(LocalDate.now(), "outra", BigDecimal.ONE))
                .isInstanceOf(IllegalStateException.class);
    }
}
