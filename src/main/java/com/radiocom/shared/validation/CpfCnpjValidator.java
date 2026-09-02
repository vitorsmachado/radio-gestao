package com.radiocom.shared.validation;

import com.radiocom.cliente.domain.model.enums.TipoPessoa;

public class CpfCnpjValidator {

    public static boolean isValid(String documento) {
        if (documento == null) return false;
        String clean = clean(documento);

        if (clean.length() == 11) {
            return CpfValidator.isValid(documento);
        } else if (clean.length() == 14) {
            return CnpjValidator.isValid(documento);
        }
        return false;
    }

    public static TipoPessoa getTipo(String documento) {
        String clean = clean(documento);
        if (clean.length() == 11) return TipoPessoa.PESSOA_FISICA;
        if (clean.length() == 14) return TipoPessoa.PESSOA_JURIDICA;
        return null;
    }

    public static String format(String documento) {
        if (documento == null) return "";
        String clean = clean(documento);

        if (clean.length() == 11) {
            return CpfValidator.format(documento);
        } else if (clean.length() == 14) {
            return CnpjValidator.format(documento);
        }
        return documento;
    }

    public static String clean(String documento) {
        if (documento == null) return "";
        return documento.replaceAll("\\D", "");
    }
}
