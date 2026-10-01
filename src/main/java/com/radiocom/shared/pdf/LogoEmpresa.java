package com.radiocom.shared.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Logo do cabeçalho dos documentos (OS, orçamento), já em base64 pro
 * template. O arquivo vem de {@code empresa.logo} — por padrão a logo
 * empacotada no jar, mas qualquer perfil/instalação pode apontar outro
 * (ex.: {@code file:/caminho/logo.png}) sem mexer no código.
 */
@Component
public class LogoEmpresa {

    private final Resource arquivo;
    private String base64Cache;

    public LogoEmpresa(@Value("${empresa.logo}") Resource arquivo) {
        this.arquivo = arquivo;
    }

    public String base64() {
        if (base64Cache != null) return base64Cache;
        try (InputStream in = arquivo.getInputStream()) {
            base64Cache = Base64.getEncoder().encodeToString(in.readAllBytes());
            return base64Cache;
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao carregar a logo da empresa: " + arquivo, e);
        }
    }
}
