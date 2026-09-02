package com.radiocom.shared.unit.validation;

import com.radiocom.shared.validation.CnpjValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CnpjValidator - Testes Unitários")
class CnpjValidatorTest {

    @Test
    @DisplayName("isValid deve retornar true para CNPJ válido")
    void isValid_deveRetornarTrueParaCnpjValido() {
        assertThat(CnpjValidator.isValid("11222333000181")).isTrue();
        assertThat(CnpjValidator.isValid("11.222.333/0001-81")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111111", "00000000000000", "11222333000180", "1122233300018", "112223330001811"})
    @DisplayName("isValid deve retornar false para CNPJ inválido")
    void isValid_deveRetornarFalseParaCnpjInvalido(String cnpj) {
        assertThat(CnpjValidator.isValid(cnpj)).isFalse();
    }

    @Test
    @DisplayName("isValid deve retornar false para nulo")
    void isValid_deveRetornarFalseParaNulo() {
        assertThat(CnpjValidator.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("format deve formatar CNPJ válido com máscara")
    void format_deveFormatarCnpjValido() {
        assertThat(CnpjValidator.format("11222333000181")).isEqualTo("11.222.333/0001-81");
    }

    @Test
    @DisplayName("format deve retornar valor original quando CNPJ inválido")
    void format_deveRetornarOriginalQuandoInvalido() {
        assertThat(CnpjValidator.format("123")).isEqualTo("123");
    }
}
