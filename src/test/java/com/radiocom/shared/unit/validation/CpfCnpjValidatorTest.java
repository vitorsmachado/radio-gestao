package com.radiocom.shared.unit.validation;

import com.radiocom.cliente.domain.model.enums.TipoPessoa;
import com.radiocom.shared.validation.CpfCnpjValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CpfCnpjValidator - Testes Unitários")
class CpfCnpjValidatorTest {

    @Test
    @DisplayName("isValid deve delegar para CpfValidator quando 11 dígitos")
    void isValid_deveDelegarParaCpfComOnzeDigitos() {
        assertThat(CpfCnpjValidator.isValid("11144477735")).isTrue();
        assertThat(CpfCnpjValidator.isValid("11144477736")).isFalse();
    }

    @Test
    @DisplayName("isValid deve delegar para CnpjValidator quando 14 dígitos")
    void isValid_deveDelegarParaCnpjComQuatorzeDigitos() {
        assertThat(CpfCnpjValidator.isValid("11222333000181")).isTrue();
        assertThat(CpfCnpjValidator.isValid("11222333000180")).isFalse();
    }

    @Test
    @DisplayName("isValid deve retornar false para tamanho diferente de 11 ou 14")
    void isValid_deveRetornarFalseParaTamanhoInvalido() {
        assertThat(CpfCnpjValidator.isValid("123")).isFalse();
    }

    @Test
    @DisplayName("getTipo deve identificar PESSOA_FISICA para CPF")
    void getTipo_deveIdentificarPessoaFisica() {
        assertThat(CpfCnpjValidator.getTipo("11144477735")).isEqualTo(TipoPessoa.PESSOA_FISICA);
    }

    @Test
    @DisplayName("getTipo deve identificar PESSOA_JURIDICA para CNPJ")
    void getTipo_deveIdentificarPessoaJuridica() {
        assertThat(CpfCnpjValidator.getTipo("11222333000181")).isEqualTo(TipoPessoa.PESSOA_JURIDICA);
    }

    @Test
    @DisplayName("clean deve remover caracteres não numéricos")
    void clean_deveRemoverCaracteresNaoNumericos() {
        assertThat(CpfCnpjValidator.clean("111.444.777-35")).isEqualTo("11144477735");
    }

    @Test
    @DisplayName("format deve aplicar máscara de acordo com o tamanho do documento")
    void format_deveAplicarMascaraCorreta() {
        assertThat(CpfCnpjValidator.format("11144477735")).isEqualTo("111.444.777-35");
        assertThat(CpfCnpjValidator.format("11222333000181")).isEqualTo("11.222.333/0001-81");
    }
}
