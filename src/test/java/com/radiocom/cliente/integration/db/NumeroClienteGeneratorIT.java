package com.radiocom.cliente.integration.db;

import com.radiocom.cliente.domain.service.NumeroClienteGenerator;
import com.radiocom.shared.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A query nativa de {@link NumeroClienteGenerator} (nextval de sequence) nunca
 * era exercitada por nenhum teste — só mockada nos testes de
 * {@code ClienteApplicationService}. Aqui ela roda de verdade contra Postgres.
 */
@DisplayName("NumeroClienteGenerator - Teste de Integração (Postgres real)")
class NumeroClienteGeneratorIT extends PostgresIntegrationTestBase {

    @Autowired
    private NumeroClienteGenerator generator;

    @Test
    @DisplayName("gerarNumero deve gerar números únicos e sequenciais")
    void gerarNumero_deveGerarNumerosUnicosESequenciais() {
        Integer primeiro = generator.gerarNumero();
        Integer segundo = generator.gerarNumero();

        assertThat(segundo).isGreaterThan(primeiro);
    }
}
