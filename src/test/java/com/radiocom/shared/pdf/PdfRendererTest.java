package com.radiocom.shared.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PdfRenderer - Testes Unitários")
class PdfRendererTest {

    private final PdfRenderer renderer = new PdfRenderer();

    @Test
    @DisplayName("renderizar deve gerar bytes de PDF válidos a partir de XHTML simples")
    void renderizar_deveGerarPdfValido() {
        String xhtml = "<html xmlns=\"http://www.w3.org/1999/xhtml\">"
                + "<head><title>Teste</title></head>"
                + "<body><p>Conteúdo de teste</p></body></html>";

        byte[] pdf = renderer.renderizar(xhtml);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("renderizar deve lançar exceção quando o XHTML é inválido")
    void renderizar_deveLancarExcecaoQuandoXhtmlInvalido() {
        assertThatThrownBy(() -> renderer.renderizar("<html><body>tag não fechada"))
                .isInstanceOf(IllegalStateException.class);
    }
}
