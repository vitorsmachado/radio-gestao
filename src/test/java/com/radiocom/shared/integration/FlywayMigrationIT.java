package com.radiocom.shared.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sobe o contexto inteiro contra um Postgres real via Testcontainers. Se as
 * migrations (V1 a V5) tiverem qualquer erro de SQL, ou se alguma entidade
 * JPA não bater com o schema que elas criam (ddl-auto=validate na base),
 * o contexto nem sobe — por isso o teste em si é simples, a validação real
 * já aconteceu no boot do @SpringBootTest.
 */
@DisplayName("Migrations Flyway - Teste de Integração (Postgres real)")
class FlywayMigrationIT extends PostgresIntegrationTestBase {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("contexto deve subir aplicando todas as migrations e validando o schema")
    void contextLoads() {
        assertThat(dataSource).isNotNull();
    }

    @Test
    @DisplayName("flyway_schema_history deve registrar as 5 migrations aplicadas com sucesso, em ordem")
    void flywayHistory_deveRegistrarTodasAsMigrationsComSucesso() throws Exception {
        List<String> versoes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT version, success FROM flyway_schema_history WHERE version IS NOT NULL ORDER BY installed_rank")) {
            while (rs.next()) {
                assertThat(rs.getBoolean("success")).isTrue();
                versoes.add(rs.getString("version"));
            }
        }

        assertThat(versoes).containsExactly("1", "2", "3", "4", "5");
    }
}
