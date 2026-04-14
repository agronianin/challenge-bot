package su.msk.nlx2.challengebot.service.bot.keyboard;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.model.UserReminder;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserKeyboardFactory {
    public static final String DELETE_REMINDER_CALLBACK_PREFIX = "delete_reminder:";
    public static final String SET_LOCALE_CALLBACK_PREFIX = "set_locale:";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BotMessages botMessages;

    public ReplyKeyboardMarkup mainMenu(Locale locale, boolean admin) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(setMaxPullUpsLabel(locale), addReminderLabel(locale))
                .addRow(showRemindersLabel(locale), changeLanguageLabel(locale));

        if (admin) {
            keyboard.addRow(botMessages.text(locale, "menu.admin.section"));
        }

        return keyboard.resizeKeyboard(true).isPersistent(true);
    }

    public InlineKeyboardMarkup languageKeyboard(Locale locale) {
        return new InlineKeyboardMarkup()
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "locale.option.ru"))
                                .callbackData(SET_LOCALE_CALLBACK_PREFIX + "ru"),
                        new InlineKeyboardButton(botMessages.text(locale, "locale.option.en"))
                                .callbackData(SET_LOCALE_CALLBACK_PREFIX + "en")
                );
    }

    public InlineKeyboardMarkup remindersKeyboard(Locale locale, List<UserReminder> reminders) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        for (UserReminder reminder : reminders) {
            markup.addRow(
                    new InlineKeyboardButton(botMessages.text(locale, "user.reminder.delete_button", formatReminder(reminder)))
                            .callbackData(DELETE_REMINDER_CALLBACK_PREFIX + reminder.getId())
            );
        }
        return markup;
    }

    public String setMaxPullUpsLabel(Locale locale) {
        return botMessages.text(locale, "menu.user.max_pull_ups");
    }

    public String addReminderLabel(Locale locale) {
        return botMessages.text(locale, "menu.user.add_reminder");
    }

    public String showRemindersLabel(Locale locale) {
        return botMessages.text(locale, "menu.user.show_reminders");
    }

    public String changeLanguageLabel(Locale locale) {
        return botMessages.text(locale, "menu.user.change_language");
    }

    public static String formatReminder(UserReminder reminder) {
        return reminder.getRemindTime().format(TIME_FORMATTER);
    }
}
