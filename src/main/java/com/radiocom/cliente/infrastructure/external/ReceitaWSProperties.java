package com.radiocom.cliente.infrastructure.external;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "integracao.receitaws")
@Getter
@Setter
public class ReceitaWSProperties {

    private String url = "https://www.receitaws.com.br/v1/cnpj/";
    private int timeout = 10000;
    private boolean habilitado = true;
}
