package su.msk.nlx2.challengebot.service.bot.message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class MessageParsingUtils {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private MessageParsingUtils() {
    }

    public static Integer parsePositiveInteger(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer parseNonNegativeInteger(String text) {
        if (text == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalTime parseTime(String text) {
        if (text == null || !text.matches("^(?:[01]\\d|2[0-3]):[0-5]\\d$")) {
            return null;
        }
        return LocalTime.parse(text, TIME_FORMATTER);
    }
}
