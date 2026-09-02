package com.radiocom.shared.validation;

public class CnpjValidator {

    private static final int[] WEIGHT_FIRST = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] WEIGHT_SECOND = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public static boolean isValid(String cnpj) {
        if (cnpj == null) return false;

        String clean = CpfCnpjValidator.clean(cnpj);

        if (clean.length() != 14) return false;
        if (isRepeatedDigits(clean)) return false;

        try {
            int firstDigit = calculateDigit(clean.substring(0, 12), WEIGHT_FIRST);
            int secondDigit = calculateDigit(clean.substring(0, 13), WEIGHT_SECOND);

            return clean.charAt(12) - '0' == firstDigit &&
                    clean.charAt(13) - '0' == secondDigit;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int calculateDigit(String str, int[] weight) {
        int sum = 0;
        for (int i = 0; i < str.length(); i++) {
            int digit = str.charAt(i) - '0';
            sum += digit * weight[i];
        }
        int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }

    private static boolean isRepeatedDigits(String cnpj) {
        return cnpj.chars().allMatch(ch -> ch == cnpj.charAt(0));
    }

    public static String format(String cnpj) {
        if (!isValid(cnpj)) return cnpj;
        String clean = CpfCnpjValidator.clean(cnpj);
        return clean.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})",
                "$1.$2.$3/$4-$5");
    }
}
