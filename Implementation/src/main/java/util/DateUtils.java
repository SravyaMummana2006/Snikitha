package util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Standard date formatting and manipulation utilities for temporal grouping.
 */
public final class DateUtils {

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    private DateUtils() {
        // Prevent instantiation
    }

    public static LocalDate parseIsoDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static YearMonth parseYearMonth(String yearMonthStr) {
        if (yearMonthStr == null || yearMonthStr.trim().isEmpty()) {
            return null;
        }
        try {
            return YearMonth.parse(yearMonthStr.trim(), YEAR_MONTH_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static String toYearMonthString(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(YEAR_MONTH_FORMATTER);
    }

    public static String toYearMonthString(String dateStr) {
        LocalDate parsed = parseIsoDate(dateStr);
        return parsed != null ? toYearMonthString(parsed) : "";
    }

    public static String toDisplayDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DISPLAY_FORMATTER);
    }
}
