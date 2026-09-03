package com.radiocom.estoque.unit.domain.model;

import com.radiocom.estoque.domain.model.Peca;
import com.radiocom.estoque.domain.model.enums.TipoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Peca - Testes de Domínio")
class PecaTest {

    private Peca peca;

    @BeforeEach
    void setUp() {
        peca = Peca.builder()
                .codigo("PC-001")
                .descricao("Bateria BP-227")
                .tipo(TipoItem.PECA)
                .quantidadeDisponivel(10)
                .build();
    }

    @Test
    @DisplayName("possuiNumeroSerie deve sempre retornar false")
    void possuiNumeroSerie_sempreFalse() {
        assertThat(peca.possuiNumeroSerie()).isFalse();
    }

    @Test
    @DisplayName("possuiPatrimonio deve sempre retornar false")
    void possuiPatrimonio_sempreFalse() {
        assertThat(peca.possuiPatrimonio()).isFalse();
    }

    @Test
    @DisplayName("entrada deve aumentar a quantidade disponível")
    void entrada_deveAumentarQuantidade() {
        peca.entrada(5);
        assertThat(peca.getQuantidadeDisponivel()).isEqualTo(15);
    }

    @Test
    @DisplayName("saida deve diminuir a quantidade disponível")
    void saida_deveDiminuirQuantidade() {
        peca.saida(4);
        assertThat(peca.getQuantidadeDisponivel()).isEqualTo(6);
    }

    @Test
    @DisplayName("saida deve lançar exceção quando quantidade insuficiente")
    void saida_deveLancarExcecaoQuandoInsuficiente() {
        assertThatThrownBy(() -> peca.saida(20))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("indisponível");
    }

    @Test
    @DisplayName("ajustar deve definir a quantidade exata")
    void ajustar_deveDefinirQuantidadeExata() {
        peca.ajustar(3);
        assertThat(peca.getQuantidadeDisponivel()).isEqualTo(3);
    }

    @Test
    @DisplayName("ajustar deve lançar exceção quando quantidade negativa")
    void ajustar_deveLancarExcecaoQuandoNegativa() {
        assertThatThrownBy(() -> peca.ajustar(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser nula ou negativa");
    }
}
