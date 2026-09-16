package com.radiocom.cliente.infrastructure.external;

import com.radiocom.cliente.infrastructure.external.dto.ReceitaWSResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * Cliente HTTP simples pra ReceitaWS — sem retry/circuit breaker (projeto
 * lean, sem dependência de resilience4j). Qualquer falha de rede, timeout ou
 * CNPJ não encontrado vira {@link Optional#empty()}: quem chama trata a
 * ausência de dados como "sem preenchimento automático", nunca como erro
 * fatal do cadastro de cliente.
 */
@Component
@Slf4j
public class ReceitaWSClient {

    private final ReceitaWSProperties properties;
    private final RestTemplate restTemplate;

    public ReceitaWSClient(ReceitaWSProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeout()))
                .build();
    }

    public Optional<ReceitaWSResponse> consultarCNPJ(String cnpj) {
        if (!properties.isHabilitado()) {
            log.warn("Consulta ReceitaWS desabilitada");
            return Optional.empty();
        }
        if (cnpj == null || cnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ deve ter 14 dígitos");
        }

        String url = properties.getUrl() + cnpj;
        log.info("Consultando ReceitaWS para CNPJ: {}", cnpj);

        try {
            ResponseEntity<ReceitaWSResponse> response = restTemplate.getForEntity(url, ReceitaWSResponse.class);
            ReceitaWSResponse dados = response.getBody();

            if (dados == null || "ERROR".equals(dados.getStatus())) {
                log.warn("ReceitaWS não encontrou dados para CNPJ {}: {}",
                        cnpj, dados != null ? dados.getMessage() : "resposta vazia");
                return Optional.empty();
            }

            return Optional.of(dados);
        } catch (Exception e) {
            log.warn("Falha ao consultar ReceitaWS para CNPJ {}: {}", cnpj, e.getMessage());
            return Optional.empty();
        }
    }
}
