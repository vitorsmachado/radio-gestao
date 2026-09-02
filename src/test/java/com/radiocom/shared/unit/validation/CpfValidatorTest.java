package com.radiocom.shared.unit.validation;

import com.radiocom.shared.validation.CpfValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CpfValidator - Testes Unitários")
class CpfValidatorTest {

    @Test
    @DisplayName("isValid deve retornar true para CPF válido")
    void isValid_deveRetornarTrueParaCpfValido() {
        assertThat(CpfValidator.isValid("11144477735")).isTrue();
        assertThat(CpfValidator.isValid("111.444.777-35")).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"11111111111", "00000000000", "12345678900", "1114447773", "111444777355"})
    @DisplayName("isValid deve retornar false para CPF inválido")
    void isValid_deveRetornarFalseParaCpfInvalido(String cpf) {
        assertThat(CpfValidator.isValid(cpf)).isFalse();
    }

    @Test
    @DisplayName("isValid deve retornar false para nulo")
    void isValid_deveRetornarFalseParaNulo() {
        assertThat(CpfValidator.isValid(null)).isFalse();
    }

    @Test
    @DisplayName("format deve formatar CPF válido com máscara")
    void format_deveFormatarCpfValido() {
        assertThat(CpfValidator.format("11144477735")).isEqualTo("111.444.777-35");
    }

    @Test
    @DisplayName("format deve retornar valor original quando CPF inválido")
    void format_deveRetornarOriginalQuandoInvalido() {
        assertThat(CpfValidator.format("123")).isEqualTo("123");
    }
}
