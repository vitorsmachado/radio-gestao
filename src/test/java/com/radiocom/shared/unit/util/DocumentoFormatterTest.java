package com.radiocom.shared.unit.util;

import com.radiocom.shared.util.DocumentoFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DocumentoFormatter - Testes Unitários")
class DocumentoFormatterTest {

    @Test
    @DisplayName("formatar deve aplicar a máscara de CPF pra documento de 11 dígitos")
    void formatar_deveAplicarMascaraDeCpf() {
        assertThat(DocumentoFormatter.formatar("11144477735")).isEqualTo("111.444.777-35");
    }

    @Test
    @DisplayName("formatar deve aplicar a máscara de CNPJ pra documento de 14 dígitos")
    void formatar_deveAplicarMascaraDeCnpj() {
        assertThat(DocumentoFormatter.formatar("59273032000103")).isEqualTo("59.273.032/0001-03");
    }

    @Test
    @DisplayName("formatar deve retornar o valor original quando não tem 11 nem 14 dígitos")
    void formatar_deveRetornarOriginalQuandoTamanhoInvalido() {
        assertThat(DocumentoFormatter.formatar("123")).isEqualTo("123");
    }

    @Test
    @DisplayName("formatar deve retornar nulo quando o documento é nulo")
    void formatar_deveRetornarNuloQuandoDocumentoNulo() {
        assertThat(DocumentoFormatter.formatar(null)).isNull();
    }
}
