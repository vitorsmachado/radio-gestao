package com.radiocom.ordemservico.unit.domain.model;

import com.radiocom.ordemservico.domain.model.OrdemServico;
import com.radiocom.ordemservico.domain.model.enums.StatusOS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrdemServico - Testes de Domínio")
class OrdemServicoTest {

    private OrdemServico os;

    @BeforeEach
    void setUp() {
        os = OrdemServico.builder()
                .numero("OS-2026-0001")
                .clienteId(UUID.randomUUID())
                .build();
    }

    @Test
    @DisplayName("nova OS deve nascer ABERTA")
    void novaOS_deveNascerAberta() {
        assertThat(os.getStatus()).isEqualTo(StatusOS.ABERTA);
        assertThat(os.isEncerrada()).isFalse();
    }

    @Test
    @DisplayName("iniciarAndamento deve mudar status para EM_ANDAMENTO")
    void iniciarAndamento_deveMudarStatus() {
        os.iniciarAndamento();

        assertThat(os.getStatus()).isEqualTo(StatusOS.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("iniciarAndamento deve lançar exceção quando não está ABERTA")
    void iniciarAndamento_deveLancarExcecaoQuandoNaoAberta() {
        os.iniciarAndamento();

        assertThatThrownBy(os::iniciarAndamento)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("confirmarEntrega deve mudar status, registrar recebedor e data de conclusão")
    void confirmarEntrega_deveMudarStatusERegistrarRecebedorEData() {
        os.confirmarEntrega("Maria Souza");

        assertThat(os.getStatus()).isEqualTo(StatusOS.CONCLUIDA);
        assertThat(os.getRecebedorNome()).isEqualTo("Maria Souza");
        assertThat(os.getDataConclusao()).isNotNull();
        assertThat(os.isEncerrada()).isTrue();
    }

    @Test
    @DisplayName("confirmarEntrega deve lançar exceção quando nome do recebedor está vazio")
    void confirmarEntrega_deveLancarExcecaoQuandoRecebedorVazio() {
        assertThatThrownBy(() -> os.confirmarEntrega(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("cancelar deve mudar status e registrar motivo nas observações")
    void cancelar_deveMudarStatusERegistrarMotivo() {
        os.cancelar("Cliente desistiu");

        assertThat(os.getStatus()).isEqualTo(StatusOS.CANCELADA);
        assertThat(os.getObservacoes()).contains("Cliente desistiu");
    }

    @Test
    @DisplayName("confirmarEntrega deve lançar exceção quando já cancelada")
    void confirmarEntrega_deveLancarExcecaoQuandoJaCancelada() {
        os.cancelar("motivo");

        assertThatThrownBy(() -> os.confirmarEntrega("Maria Souza"))
                .isInstanceOf(IllegalStateException.class);
    }
}
