package com.radiocom.shared.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base para testes que precisam de um Postgres real (não H2), com as
 * migrations Flyway de fato aplicadas — usada para validar o que os testes
 * rápidos (perfil "test", H2, Flyway desligado) não conseguem: que as
 * migrations rodam sem erro e que as entidades JPA batem com o schema que
 * elas criam.
 *
 * Perfil "dev" (não "test") é usado de propósito, porque é o único com
 * Flyway habilitado; o {@code @ServiceConnection} substitui a URL do
 * datasource pela do container, então as credenciais do application-dev.yaml
 * nunca chegam a ser usadas de verdade.
 *
 * Padrão "singleton container": propositalmente SEM {@code @Testcontainers}/
 * {@code @Container} — essas anotações param o container ao final de CADA
 * classe de teste, e como o campo static é compartilhado entre
 * FlywayMigrationIT/NumeroOSGeneratorIT/NumeroOrcamentoGeneratorIT (todas
 * rodando na mesma JVM), a primeira a terminar derrubava o container pras
 * outras. Aqui ele é iniciado uma única vez à mão e nunca parado — quem
 * encerra é o Ryuk (container auxiliar do Testcontainers) quando a JVM do
 * teste termina.
 */
@SpringBootTest
@ActiveProfiles("dev")
public abstract class PostgresIntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void ddlValidate(DynamicPropertyRegistry registry) {
        // ddl-auto=validate (em vez do "none" do perfil dev): falha o
        // contexto se as entidades JPA não baterem com o schema criado
        // pelas migrations, em vez de só deixar o erro estourar em runtime.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
