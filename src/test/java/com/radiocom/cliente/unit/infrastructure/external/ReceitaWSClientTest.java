package com.radiocom.cliente.unit.infrastructure.external;

import com.radiocom.cliente.infrastructure.external.ReceitaWSClient;
import com.radiocom.cliente.infrastructure.external.ReceitaWSProperties;
import com.radiocom.cliente.infrastructure.external.dto.ReceitaWSResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * ReceitaWSClient constrói o próprio RestTemplate a partir do RestTemplateBuilder
 * injetado — MockServerRestTemplateCustomizer é o jeito padrão do Spring Boot de
 * interceptar esse RestTemplate sem precisar mudar o design do cliente.
 */
@DisplayName("ReceitaWSClient - Testes Unitários")
class ReceitaWSClientTest {

    private ReceitaWSProperties properties;
    private MockRestServiceServer server;
    private ReceitaWSClient client;

    @BeforeEach
    void setUp() {
        properties = new ReceitaWSProperties();
        MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
        RestTemplateBuilder builder = new RestTemplateBuilder().customizers(customizer);
        client = new ReceitaWSClient(properties, builder);
        server = customizer.getServer();
    }

    @Test
    @DisplayName("consultarCNPJ deve retornar os dados quando a ReceitaWS responde com sucesso")
    void consultarCNPJ_deveRetornarDados() {
        server.expect(requestTo(properties.getUrl() + "11222333000181"))
                .andRespond(withSuccess(
                        "{\"status\":\"OK\",\"nome\":\"Radio Comunicacao LTDA\",\"fantasia\":\"Radiocom\","
                                + "\"logradouro\":\"Rua X\",\"cep\":\"01310-100\"}",
                        MediaType.APPLICATION_JSON));

        Optional<ReceitaWSResponse> resultado = client.consultarCNPJ("11222333000181");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Radio Comunicacao LTDA");
        assertThat(resultado.get().getFantasia()).isEqualTo("Radiocom");
    }

    @Test
    @DisplayName("consultarCNPJ deve retornar vazio quando a ReceitaWS responde com status ERROR")
    void consultarCNPJ_deveRetornarVazioQuandoStatusError() {
        server.expect(requestTo(properties.getUrl() + "00000000000000"))
                .andRespond(withSuccess(
                        "{\"status\":\"ERROR\",\"message\":\"CNPJ inválido\"}",
                        MediaType.APPLICATION_JSON));

        assertThat(client.consultarCNPJ("00000000000000")).isEmpty();
    }

    @Test
    @DisplayName("consultarCNPJ deve retornar vazio quando a ReceitaWS está fora do ar")
    void consultarCNPJ_deveRetornarVazioQuandoServidorFalha() {
        server.expect(requestTo(properties.getUrl() + "11222333000181"))
                .andRespond(withServerError());

        assertThat(client.consultarCNPJ("11222333000181")).isEmpty();
    }

    @Test
    @DisplayName("consultarCNPJ deve retornar vazio quando a integração está desabilitada")
    void consultarCNPJ_deveRetornarVazioQuandoDesabilitada() {
        properties.setHabilitado(false);

        assertThat(client.consultarCNPJ("11222333000181")).isEmpty();
    }

    @Test
    @DisplayName("consultarCNPJ deve lançar exceção quando o CNPJ não tem 14 dígitos")
    void consultarCNPJ_deveLancarExcecaoQuandoCnpjInvalido() {
        assertThatThrownBy(() -> client.consultarCNPJ("123"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
