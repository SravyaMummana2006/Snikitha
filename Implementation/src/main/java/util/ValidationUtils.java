package util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Robust input validation utilities for CVE identifiers, dates, and text sanitization.
 */
public final class ValidationUtils {

    private static final Pattern CVE_PATTERN = Pattern.compile("^CVE-\\d{4}-\\d{4,}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private ValidationUtils() {
        // Prevent instantiation
    }

    public static boolean isValidCveId(String cveId) {
        if (cveId == null || cveId.trim().isEmpty()) {
            return false;
        }
        return CVE_PATTERN.matcher(cveId.trim()).matches();
    }

    public static boolean isValidIsoDate(String dateStr) {
        if (dateStr == null || !ISO_DATE_PATTERN.matcher(dateStr.trim()).matches()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String clean(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}
