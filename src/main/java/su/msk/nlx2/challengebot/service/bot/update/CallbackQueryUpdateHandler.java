package su.msk.nlx2.challengebot.service.bot.update;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.bot.BotUserContext;
import su.msk.nlx2.challengebot.model.type.ChallengeJoinStatus;
import su.msk.nlx2.challengebot.model.type.ProgramActionResult;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import su.msk.nlx2.challengebot.service.ChallengeParticipationService;
import su.msk.nlx2.challengebot.service.DailyRunner;
import su.msk.nlx2.challengebot.service.UserReminderService;
import su.msk.nlx2.challengebot.service.UserService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.context.BotRequestContextResolver;
import su.msk.nlx2.challengebot.service.bot.keyboard.ChallengeKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;

@Service
@RequiredArgsConstructor
public class CallbackQueryUpdateHandler {
    private final TelegramBot bot;
    private final BotMessages botMessages;
    private final UserService userService;
    private final UserReminderService userReminderService;
    private final ChallengeAdminService challengeAdminService;
    private final ChallengeParticipationService challengeParticipationService;
    private final DailyRunner dailyRunner;
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
            return;
        }
        if (data.startsWith(ChallengeKeyboardFactory.JOIN_CHALLENGE_CALLBACK_PREFIX)) {
            handleJoinChallengeCallback(callbackQuery, context);
            return;
        }
        if (context.admin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_PAUSE_CALLBACK_PREFIX)) {
            handlePauseChallengeCallback(callbackQuery, context);
            return;
        }
        if (context.admin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_DELETE_CALLBACK_PREFIX)) {
            handleDeleteChallengeRequestCallback(callbackQuery, context);
            return;
        }
        if (context.admin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_DELETE_CONFIRM_CALLBACK_PREFIX)) {
            handleDeleteChallengeConfirmCallback(callbackQuery, context);
            return;
        }
        if (context.admin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_DELETE_CANCEL_CALLBACK_PREFIX)) {
            handleDeleteChallengeCancelCallback(callbackQuery, context);
            return;
        }
        if (context.admin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX)) {
            handleRepublishLastDayCallback(callbackQuery, context);
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

    private void handleJoinChallengeCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Integer programId = parseId(callbackQuery.data(), ChallengeKeyboardFactory.JOIN_CHALLENGE_CALLBACK_PREFIX);
        if (programId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "challenge.join.not_found")));
            return;
        }
        var result = challengeParticipationService.join(context.user(), programId);
        if (result.status() == ChallengeJoinStatus.SUCCESS || result.status() == ChallengeJoinStatus.ALREADY_JOINED) {
            boolean sent = dailyRunner.sendCurrentPublishedDayToUser(programId, context.user(), context.locale());
            String key = sent ? "challenge.join.success" : "challenge.join.success_no_published_day";
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), key)));
            return;
        }
        String key = switch (result.status()) {
            case SETUP_REQUIRED -> "challenge.join.setup_required";
            case PROGRAM_NOT_ACTIVE -> "challenge.join.not_active";
            case PROGRAM_NOT_FOUND -> "challenge.join.not_found";
            case SUCCESS, ALREADY_JOINED -> "challenge.join.success";
        };
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), key)));
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
        boolean isAdmin = user.getRole() == su.msk.nlx2.challengebot.model.type.UserRole.ADMIN;
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(locale, "locale.changed")));
        privateChatUpdateHandler.showMainMenu(chatId, isAdmin, locale);
    }

    private void handlePauseChallengeCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_PAUSE_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "admin.challenge.pause.not_found")));
            return;
        }
        ProgramActionResult result = challengeAdminService.pauseProgram(challengeId);
        String text = switch (result) {
            case SUCCESS -> botMessages.text(context.locale(), "admin.challenge.pause.success", challengeId);
            case NO_CHANGES -> botMessages.text(context.locale(), "admin.challenge.pause.already_paused", challengeId);
            case NOT_FOUND -> botMessages.text(context.locale(), "admin.challenge.pause.not_found", challengeId);
        };
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showManageChallenges(chatId, context.locale());
    }

    private void handleDeleteChallengeRequestCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_DELETE_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "admin.challenge.delete.not_found")));
            return;
        }
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "admin.challenge.delete.confirm_callback")));
        privateChatUpdateHandler.showMainMenu(chatId, true, context.locale());
        privateChatUpdateHandler.showDeleteChallengeConfirmation(chatId, context.locale(), challengeId);
    }

    private void handleDeleteChallengeConfirmCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_DELETE_CONFIRM_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "admin.challenge.delete.not_found")));
            return;
        }
        ProgramActionResult result = challengeAdminService.deleteProgram(challengeId);
        String text = result == ProgramActionResult.SUCCESS
                ? botMessages.text(context.locale(), "admin.challenge.delete.success", challengeId)
                : botMessages.text(context.locale(), "admin.challenge.delete.not_found", challengeId);
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showManageChallenges(chatId, context.locale());
    }

    private void handleDeleteChallengeCancelCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "common.action_cancelled")));
        if (chatId != null) {
            privateChatUpdateHandler.showManageChallenges(chatId, context.locale());
        }
    }

    private void handleRepublishLastDayCallback(CallbackQuery callbackQuery, BotUserContext context) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(context.locale(), "admin.challenge.republish.not_found")));
            return;
        }
        ProgramActionResult result = dailyRunner.republishLastPublishedDay(challengeId);
        String text = switch (result) {
            case SUCCESS -> botMessages.text(context.locale(), "admin.challenge.republish.success", challengeId);
            case NO_CHANGES -> botMessages.text(context.locale(), "admin.challenge.republish.no_published_day", challengeId);
            case NOT_FOUND -> botMessages.text(context.locale(), "admin.challenge.republish.not_found", challengeId);
        };
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showManageChallenges(chatId, context.locale());
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

    private Integer parseId(String callbackData, String prefix) {
        if (callbackData == null || !callbackData.startsWith(prefix)) {
            return null;
        }
        try {
            return Integer.parseInt(callbackData.substring(prefix.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
