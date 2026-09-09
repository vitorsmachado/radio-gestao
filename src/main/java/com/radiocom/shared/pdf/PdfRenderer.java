package com.radiocom.shared.pdf;

import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

/**
 * Conversão de XHTML (já processado pelo Thymeleaf) para bytes de PDF via
 * Flying Saucer. Compartilhado entre os módulos que geram documento
 * (Ordem de Serviço, Orçamento) para não duplicar o boilerplate do
 * ITextRenderer em cada um.
 */
@Component
public class PdfRenderer {

    public byte[] renderizar(String xhtml) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar PDF", e);
        }
    }
}
