package com.radiocom.shared.validation;

public class CpfValidator {

    public static boolean isValid(String cpf) {
        if (cpf == null) return false;

        String clean = CpfCnpjValidator.clean(cpf);

        if (clean.length() != 11) return false;
        if (isRepeatedDigits(clean)) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (clean.charAt(i) - '0') * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit > 9) firstDigit = 0;

            if ((clean.charAt(9) - '0') != firstDigit) return false;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (clean.charAt(i) - '0') * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit > 9) secondDigit = 0;

            return (clean.charAt(10) - '0') == secondDigit;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isRepeatedDigits(String cpf) {
        return cpf.chars().allMatch(ch -> ch == cpf.charAt(0));
    }

    public static String format(String cpf) {
        if (!isValid(cpf)) return cpf;
        String clean = cpf.replaceAll("\\D", "");
        return clean.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

}
