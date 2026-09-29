package com.radiocom.shared.util;

/** Formata CPF/CNPJ pro padrão com pontuação, pra exibição em telas e documentos. */
public final class DocumentoFormatter {

    private DocumentoFormatter() {
    }

    public static String formatar(String documento) {
        if (documento == null) return null;
        String digitos = documento.replaceAll("\\D", "");
        if (digitos.length() == 11) {
            return digitos.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        if (digitos.length() == 14) {
            return digitos.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return documento;
    }
}
