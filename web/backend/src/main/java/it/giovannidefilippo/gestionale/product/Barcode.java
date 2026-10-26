package it.giovannidefilippo.gestionale.product;

import java.util.regex.Pattern;

/**
 * Codice a barre di prodotto. I codici numerici di 8, 12 o 13 cifre sono trattati come GTIN
 * (EAN-8, UPC-A, EAN-13) e devono avere la cifra di controllo corretta; gli altri sono codici
 * interni alfanumerici.
 */
final class Barcode {
    static final int MAX_LENGTH = 32;
    private static final Pattern ALLOWED = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]*");
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private Barcode() {
    }

    /** Restituisce il codice normalizzato, {@code null} se assente, o rifiuta un codice non valido. */
    static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String barcode = value.trim();
        if (barcode.length() > MAX_LENGTH || !ALLOWED.matcher(barcode).matches()) {
            throw new IllegalArgumentException("Codice a barre non valido: usare fino a " + MAX_LENGTH + " lettere, cifre, punto, trattino o trattino basso.");
        }
        if (isGtinLength(barcode) && !hasValidCheckDigit(barcode)) {
            throw new IllegalArgumentException("Codice a barre " + barcode + " non valido: cifra di controllo errata.");
        }
        return barcode;
    }

    static boolean isGtinLength(String barcode) {
        return DIGITS.matcher(barcode).matches()
                && (barcode.length() == 8 || barcode.length() == 12 || barcode.length() == 13);
    }

    static boolean hasValidCheckDigit(String digits) {
        int sum = 0;
        int lastIndex = digits.length() - 1;
        for (int index = lastIndex - 1, position = 0; index >= 0; index--, position++) {
            int digit = digits.charAt(index) - '0';
            sum += position % 2 == 0 ? digit * 3 : digit;
        }
        int expected = (10 - (sum % 10)) % 10;
        return expected == digits.charAt(lastIndex) - '0';
    }
}
