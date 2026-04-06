package su.msk.nlx2.challengebot.service.bot.update;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.bot.BotUserContext;
import su.msk.nlx2.challengebot.model.type.UserRole;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.UserReminderService;
import su.msk.nlx2.challengebot.service.UserService;
import su.msk.nlx2.challengebot.service.bot.context.BotRequestContextResolver;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;

@Service
@RequiredArgsConstructor
public class CallbackQueryUpdateHandler {
    private final TelegramBot bot;
    private final BotMessages botMessages;
    private final UserService userService;
    private final UserReminderService userReminderService;
    private final BotRequestContextResolver botRequestContextResolver;
    private final PrivateChatUpdateHandler privateChatUpdateHandler;

    public void handle(CallbackQuery callbackQuery) {
        BotUserContext context = botRequestContextResolver.resolve(callbackQuery).orElse(null);
        if (context == null || callbackQuery.data() == null) {
            return;
        }
        String data = callbackQuery.data();
        if (data.startsWith(UserKeyboardFactory.SET_LOCALE_CALLBACK_PREFIX)) {
            handleSetLocaleCallback(callbackQuery, context);
            return;
        }
        if (data.startsWith(UserKeyboardFactory.DELETE_REMINDER_CALLBACK_PREFIX)) {
            handleDeleteReminderCallback(callbackQuery, context);
        }
    }

    private void handleDeleteReminderCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        Integer reminderId = parseReminderId(callbackQuery.data());
        if (chatId == null || reminderId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "user.reminder.delete_failed")));
            return;
        }
        boolean deleted = userReminderService.deleteReminder(context.tgUserId(), reminderId);
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(deleted ? botMessages.text(context.locale(), "user.reminder.deleted") : botMessages.text(context.locale(), "user.reminder.not_found")));
        privateChatUpdateHandler.showUserReminders(chatId, context);
    }

    private void handleSetLocaleCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        String localeCode = parseLocaleCode(callbackQuery.data());
        if (chatId == null || localeCode == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text("Invalid locale selection."));
            return;
        }
        var user = userService.setLocale(context.tgUserId(), localeCode);
        Locale locale = botMessages.resolveLocale(user, callbackQuery.from().languageCode());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(locale, "locale.changed")));
        privateChatUpdateHandler.showMainMenu(chatId, isAdmin, locale);
    }

    private Integer parseReminderId(String callbackData) {
        if (callbackData == null || !callbackData.startsWith(UserKeyboardFactory.DELETE_REMINDER_CALLBACK_PREFIX)) {
            return null;
        }
        try {
            return Integer.parseInt(callbackData.substring(UserKeyboardFactory.DELETE_REMINDER_CALLBACK_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String parseLocaleCode(String callbackData) {
        if (callbackData == null || !callbackData.startsWith(UserKeyboardFactory.SET_LOCALE_CALLBACK_PREFIX)) {
            return null;
        }
        String localeCode = callbackData.substring(UserKeyboardFactory.SET_LOCALE_CALLBACK_PREFIX.length()).trim().toLowerCase(Locale.ROOT);
        return ("ru".equals(localeCode) || "en".equals(localeCode)) ? localeCode : null;
    }
}
