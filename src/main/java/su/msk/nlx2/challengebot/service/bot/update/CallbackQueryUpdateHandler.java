package su.msk.nlx2.challengebot.service.bot.update;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.TgUser;
import su.msk.nlx2.challengebot.model.bot.ChallengeJoinResult;
import su.msk.nlx2.challengebot.model.type.ChallengeJoinStatus;
import su.msk.nlx2.challengebot.model.type.ProgramActionResult;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import su.msk.nlx2.challengebot.service.ChallengeParticipationService;
import su.msk.nlx2.challengebot.service.DailyRunner;
import su.msk.nlx2.challengebot.service.UserReminderService;
import su.msk.nlx2.challengebot.service.UserService;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.callback.admin.AdminMenuActionContext;
import su.msk.nlx2.challengebot.service.bot.callback.admin.AdminMenuActionHandlerProvider;
import su.msk.nlx2.challengebot.service.bot.callback.user.UserMenuActionContext;
import su.msk.nlx2.challengebot.service.bot.callback.user.UserMenuActionHandlerProvider;
import su.msk.nlx2.challengebot.service.bot.context.BotRequestContextResolver;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
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
    private final MessageSender messageSender;
    private final BotRequestContextResolver botRequestContextResolver;
    private final PrivateChatUpdateHandler privateChatUpdateHandler;
    private final UserMenuActionHandlerProvider userMenuActionHandlerProvider;
    private final AdminMenuActionHandlerProvider adminMenuActionHandlerProvider;

    public void handle(CallbackQuery callbackQuery) {
        TgUser user = botRequestContextResolver.resolve(callbackQuery).orElse(null);
        if (user == null || callbackQuery.data() == null) {
            return;
        }
        String data = callbackQuery.data();
        Long privateChatId = extractPrivateChatId(callbackQuery);
        if (privateChatId != null && userMenuActionHandlerProvider.handles(data)) {
            userMenuActionHandlerProvider.handle(data, new UserMenuActionContext(privateChatId, user));
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            return;
        }
        if (privateChatId != null && user.isAdmin() && adminMenuActionHandlerProvider.handles(data)) {
            adminMenuActionHandlerProvider.handle(data, new AdminMenuActionContext(privateChatId, user));
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()));
            return;
        }
        if (data.startsWith(UserKeyboardFactory.SET_LOCALE_CALLBACK_PREFIX)) {
            handleSetLocaleCallback(callbackQuery, user);
            return;
        }
        if (data.startsWith(UserKeyboardFactory.DELETE_REMINDER_CALLBACK_PREFIX)) {
            handleDeleteReminderCallback(callbackQuery, user);
            return;
        }
        if (data.startsWith(ChallengeKeyboardFactory.JOIN_CHALLENGE_CALLBACK_PREFIX)) {
            handleJoinChallengeCallback(callbackQuery, user);
            return;
        }
        if (user.isAdmin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_PAUSE_CALLBACK_PREFIX)) {
            handlePauseChallengeCallback(callbackQuery, user);
            return;
        }
        if (user.isAdmin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_DELETE_CALLBACK_PREFIX)) {
            handleDeleteChallengeRequestCallback(callbackQuery, user);
            return;
        }
        if (user.isAdmin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_DELETE_CONFIRM_CALLBACK_PREFIX)) {
            handleDeleteChallengeConfirmCallback(callbackQuery, user);
            return;
        }
        if (user.isAdmin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_DELETE_CANCEL_CALLBACK_PREFIX)) {
            handleDeleteChallengeCancelCallback(callbackQuery, user);
            return;
        }
        if (user.isAdmin() && data.startsWith(AdminKeyboardFactory.CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX)) {
            handleRepublishLastDayCallback(callbackQuery, user);
        }
    }

    private void handleDeleteReminderCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.maybeInaccessibleMessage().chat().id();
        Integer reminderId = parseReminderId(callbackQuery.data());
        if (chatId == null || reminderId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "user.reminder.delete_failed")));
            return;
        }
        boolean deleted = userReminderService.deleteReminder(user.getTgId(), reminderId);
        String text = deleted
                ? botMessages.text(user.getLocale(), "user.reminder.deleted")
                : botMessages.text(user.getLocale(), "user.reminder.not_found");
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showUserReminders(chatId, user);
    }

    private void handleJoinChallengeCallback(CallbackQuery callbackQuery, TgUser user) {
        Integer programId = parseId(callbackQuery.data(), ChallengeKeyboardFactory.JOIN_CHALLENGE_CALLBACK_PREFIX);
        if (programId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "challenge.join.not_found")));
            return;
        }
        ChallengeJoinResult result = challengeParticipationService.join(user, programId);
        if (result.status() == ChallengeJoinStatus.ALREADY_ACTIVE_PARTICIPATION) {
            user.setActiveParticipant(true);
            String text = botMessages.text(user.getLocale(), "challenge.join.already_active_participation");
            messageSender.sendText(user.getTgId(), text);
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
            privateChatUpdateHandler.showMainMenu(user.getTgId(), user);
            return;
        }
        if (result.status() == ChallengeJoinStatus.SUCCESS) {
            user.setActiveParticipant(true);
            boolean sent = dailyRunner.sendCurrentPublishedDayToUser(programId, user, user.getLocale());
            String key = sent ? "challenge.join.success" : "challenge.join.success_no_published_day";
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), key)));
            privateChatUpdateHandler.showMainMenu(user.getTgId(), user);
            return;
        }
        String key = switch (result.status()) {
            case SETUP_REQUIRED -> "challenge.join.setup_required";
            case PROGRAM_NOT_ACTIVE -> "challenge.join.not_active";
            case PROGRAM_NOT_FOUND -> "challenge.join.not_found";
            case SUCCESS -> "challenge.join.success";
            case ALREADY_ACTIVE_PARTICIPATION -> "challenge.join.already_active_participation";
        };
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), key)));
    }

    private void handleSetLocaleCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.message().chat().id();
        String localeCode = parseLocaleCode(callbackQuery.data());
        if (chatId == null || localeCode == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text("Invalid locale selection."));
            return;
        }
        TgUser updatedUser = userService.setLocale(user.getTgId(), localeCode);
        Locale locale = botMessages.resolveLocale(updatedUser, callbackQuery.from().languageCode());
        updatedUser.setLocale(locale);
        updatedUser.setActiveParticipant(user.isActiveParticipant());
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(locale, "locale.changed")));
        privateChatUpdateHandler.showMainMenu(chatId, updatedUser);
    }

    private void handlePauseChallengeCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_PAUSE_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "admin.challenge.pause.not_found")));
            return;
        }
        ProgramActionResult result = challengeAdminService.pauseProgram(challengeId);
        String text = switch (result) {
            case SUCCESS -> botMessages.text(user.getLocale(), "admin.challenge.pause.success", challengeId);
            case NO_CHANGES -> botMessages.text(user.getLocale(), "admin.challenge.pause.already_paused", challengeId);
            case NOT_FOUND -> botMessages.text(user.getLocale(), "admin.challenge.pause.not_found", challengeId);
        };
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showManageChallenges(chatId, user.getLocale());
    }

    private void handleDeleteChallengeRequestCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_DELETE_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "admin.challenge.delete.not_found")));
            return;
        }
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "admin.challenge.delete.confirm_callback")));
        privateChatUpdateHandler.showMainMenu(chatId, user);
        privateChatUpdateHandler.showDeleteChallengeConfirmation(chatId, user.getLocale(), challengeId);
    }

    private void handleDeleteChallengeConfirmCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_DELETE_CONFIRM_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "admin.challenge.delete.not_found")));
            return;
        }
        ProgramActionResult result = challengeAdminService.deleteProgram(challengeId);
        String text = result == ProgramActionResult.SUCCESS
                ? botMessages.text(user.getLocale(), "admin.challenge.delete.success", challengeId)
                : botMessages.text(user.getLocale(), "admin.challenge.delete.not_found", challengeId);
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showManageChallenges(chatId, user.getLocale());
    }

    private void handleDeleteChallengeCancelCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.message().chat().id();
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "common.action_cancelled")));
        if (chatId != null) {
            privateChatUpdateHandler.showManageChallenges(chatId, user.getLocale());
        }
    }

    private void handleRepublishLastDayCallback(CallbackQuery callbackQuery, TgUser user) {
        Long chatId = callbackQuery.message().chat().id();
        Integer challengeId = parseId(callbackQuery.data(), AdminKeyboardFactory.CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX);
        if (chatId == null || challengeId == null) {
            bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(botMessages.text(user.getLocale(), "admin.challenge.republish.not_found")));
            return;
        }
        ProgramActionResult result = dailyRunner.republishLastPublishedDay(challengeId);
        String text = switch (result) {
            case SUCCESS -> botMessages.text(user.getLocale(), "admin.challenge.republish.success", challengeId);
            case NO_CHANGES -> botMessages.text(user.getLocale(), "admin.challenge.republish.no_published_day", challengeId);
            case NOT_FOUND -> botMessages.text(user.getLocale(), "admin.challenge.republish.not_found", challengeId);
        };
        bot.execute(new AnswerCallbackQuery(callbackQuery.id()).text(text));
        privateChatUpdateHandler.showManageChallenges(chatId, user.getLocale());
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

    private Long extractPrivateChatId(CallbackQuery callbackQuery) {
        if (callbackQuery.message() == null || callbackQuery.message().chat() == null) {
            return null;
        }
        return callbackQuery.message().chat().type() == Chat.Type.Private ? callbackQuery.message().chat().id() : null;
    }
}
