package com.radiocom.orcamento.integration.db;

import com.radiocom.orcamento.domain.service.NumeroOrcamentoGenerator;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mesma lógica de {@link com.radiocom.ordemservico.integration.db.NumeroOSGeneratorIT},
 * para o gerador de número de orçamento.
 */
@DisplayName("NumeroOrcamentoGenerator - Teste de Integração (Postgres real)")
class NumeroOrcamentoGeneratorIT extends PostgresIntegrationTestBase {

    @Autowired
    private NumeroOrcamentoGenerator generator;

    @Test
    @DisplayName("gerarNumero deve gerar números únicos e sequenciais no formato ORC-ANO-NNNN")
    void gerarNumero_deveGerarNumerosUnicosESequenciais() {
        String primeiro = generator.gerarNumero();
        String segundo = generator.gerarNumero();

        int ano = LocalDate.now().getYear();
        assertThat(primeiro).matches("ORC-" + ano + "-\\d{4}");
        assertThat(segundo).matches("ORC-" + ano + "-\\d{4}");
        assertThat(primeiro).isNotEqualTo(segundo);
    }
}
