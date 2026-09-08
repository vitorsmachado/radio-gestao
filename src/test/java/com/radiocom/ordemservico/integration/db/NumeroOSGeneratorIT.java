package com.radiocom.ordemservico.integration.db;

import com.radiocom.ordemservico.domain.service.NumeroOSGenerator;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A query nativa de {@link NumeroOSGenerator} (criação de sequence por ano +
 * nextval) nunca era exercitada por nenhum teste — só mockada nos testes de
 * {@code OrdemServicoDomainService}. Aqui ela roda de verdade contra Postgres.
 */
@DisplayName("NumeroOSGenerator - Teste de Integração (Postgres real)")
class NumeroOSGeneratorIT extends PostgresIntegrationTestBase {

    @Autowired
    private NumeroOSGenerator generator;

    @Test
    @DisplayName("gerarNumero deve gerar números únicos e sequenciais no formato OS-ANO-NNNN")
    void gerarNumero_deveGerarNumerosUnicosESequenciais() {
        String primeiro = generator.gerarNumero();
        String segundo = generator.gerarNumero();

        int ano = LocalDate.now().getYear();
        assertThat(primeiro).matches("OS-" + ano + "-\\d{4}");
        assertThat(segundo).matches("OS-" + ano + "-\\d{4}");
        assertThat(primeiro).isNotEqualTo(segundo);
    }
}
