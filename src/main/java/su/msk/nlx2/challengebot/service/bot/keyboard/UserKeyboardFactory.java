package su.msk.nlx2.challengebot.service.bot.keyboard;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.UserReminder;
import su.msk.nlx2.challengebot.service.BotMessages;

@Component
@RequiredArgsConstructor
public class UserKeyboardFactory {

    public static final String USER_MENU_OPEN_MAIN_CALLBACK = "user_menu:open_main";
    public static final String USER_MENU_SET_MAX_PULL_UPS_CALLBACK = "user_menu:set_max_pull_ups";
    public static final String USER_MENU_ADD_REMINDER_CALLBACK = "user_menu:add_reminder";
    public static final String USER_MENU_SHOW_REMINDERS_CALLBACK = "user_menu:show_reminders";
    public static final String USER_MENU_CHANGE_LANGUAGE_CALLBACK = "user_menu:change_language";
    public static final String USER_MENU_GIVE_UP_CALLBACK = "user_menu:give_up";
    public static final String USER_MENU_OPEN_ADMIN_SECTION_CALLBACK = "user_menu:open_admin_section";
    public static final String DELETE_REMINDER_CALLBACK_PREFIX = "delete_reminder:";
    public static final String SET_LOCALE_CALLBACK_PREFIX = "set_locale:";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BotMessages botMessages;

    public InlineKeyboardMarkup mainMenu(Locale locale, boolean admin, boolean activeParticipant) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup()
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "menu.user.max_pull_ups"))
                                .callbackData(USER_MENU_SET_MAX_PULL_UPS_CALLBACK),
                        new InlineKeyboardButton(botMessages.text(locale, "menu.user.add_reminder"))
                                .callbackData(USER_MENU_ADD_REMINDER_CALLBACK)
                )
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "menu.user.show_reminders"))
                                .callbackData(USER_MENU_SHOW_REMINDERS_CALLBACK),
                        new InlineKeyboardButton(botMessages.text(locale, "menu.user.change_language"))
                                .callbackData(USER_MENU_CHANGE_LANGUAGE_CALLBACK)
                );
        if (activeParticipant) {
            keyboard.addRow(
                    new InlineKeyboardButton(botMessages.text(locale, "menu.user.give_up"))
                            .callbackData(USER_MENU_GIVE_UP_CALLBACK)
            );
        }
        if (admin) {
            keyboard.addRow(
                    new InlineKeyboardButton(botMessages.text(locale, "menu.admin.section"))
                            .callbackData(USER_MENU_OPEN_ADMIN_SECTION_CALLBACK)
            );
        }
        return keyboard;
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
        markup.addRow(
                new InlineKeyboardButton(botMessages.text(locale, "menu.main"))
                        .callbackData(USER_MENU_OPEN_MAIN_CALLBACK)
        );
        return markup;
    }

    public static String formatReminder(UserReminder reminder) {
        return reminder.getRemindTime().format(TIME_FORMATTER);
    }
}
