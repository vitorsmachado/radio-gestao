package com.radiocom.ordemservico.unit.domain.model;

import com.radiocom.ordemservico.domain.model.ItemConserto;
import com.radiocom.ordemservico.domain.model.enums.TipoItemConserto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ItemConserto - Testes de Domínio")
class ItemConsertoTest {

    @Test
    @DisplayName("calcularTotal deve multiplicar valorUnitario pela quantidade")
    void calcularTotal_deveMultiplicarValorPorQuantidade() {
        ItemConserto item = ItemConserto.builder()
                .tipo(TipoItemConserto.PECA)
                .descricao("Bateria BP-227")
                .quantidade(2)
                .valorUnitario(new BigDecimal("50.00"))
                .build();

        item.calcularTotal();

        assertThat(item.getValorTotal()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("calcularTotal deve lançar exceção quando valorUnitario é nulo")
    void calcularTotal_deveLancarExcecaoQuandoValorNulo() {
        ItemConserto item = ItemConserto.builder()
                .tipo(TipoItemConserto.MAO_DE_OBRA)
                .quantidade(1)
                .build();

        assertThatThrownBy(item::calcularTotal)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("valorUnitario");
    }

    @Test
    @DisplayName("calcularTotal deve lançar exceção quando quantidade não é positiva")
    void calcularTotal_deveLancarExcecaoQuandoQuantidadeInvalida() {
        ItemConserto item = ItemConserto.builder()
                .tipo(TipoItemConserto.DESLOCAMENTO)
                .quantidade(0)
                .valorUnitario(new BigDecimal("30.00"))
                .build();

        assertThatThrownBy(item::calcularTotal)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quantidade deve ser positiva");
    }
}
