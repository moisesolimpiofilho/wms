package com.wms.domain.validation;

public final class BrazilianDocument {
    private BrazilianDocument() {
    }

    public static String normalize(String document) {
        return document == null ? null : document.replaceAll("\\D", "");
    }

    public static boolean isValid(String document) {
        if (document == null || !document.matches("[0-9.\\-/\\s]+")) {
            return false;
        }

        String digits = normalize(document);
        if (digits.length() == 11) {
            return isValidCpf(digits);
        }
        if (digits.length() == 14) {
            return isValidCnpj(digits);
        }
        return false;
    }

    private static boolean isValidCpf(String cpf) {
        if (hasRepeatedDigits(cpf)) {
            return false;
        }

        int firstDigit = calculateDigit(cpf.substring(0, 9), new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2});
        int secondDigit = calculateDigit(cpf.substring(0, 9) + firstDigit,
                new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2});
        return cpf.endsWith(firstDigit + "" + secondDigit);
    }

    private static boolean isValidCnpj(String cnpj) {
        if (hasRepeatedDigits(cnpj)) {
            return false;
        }

        int firstDigit = calculateDigit(cnpj.substring(0, 12),
                new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        int secondDigit = calculateDigit(cnpj.substring(0, 12) + firstDigit,
                new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        return cnpj.endsWith(firstDigit + "" + secondDigit);
    }

    private static int calculateDigit(String digits, int[] weights) {
        int sum = 0;
        for (int index = 0; index < weights.length; index++) {
            sum += Character.getNumericValue(digits.charAt(index)) * weights[index];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean hasRepeatedDigits(String digits) {
        return digits.chars().allMatch(digit -> digit == digits.charAt(0));
    }
}
