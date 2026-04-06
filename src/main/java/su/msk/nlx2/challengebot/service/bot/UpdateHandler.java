package su.msk.nlx2.challengebot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.shared.SharedUser;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.type.UserRole;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import su.msk.nlx2.challengebot.service.UserReminderService;
import su.msk.nlx2.challengebot.service.UserService;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerProvider;

@Component
@RequiredArgsConstructor
public class UpdateHandler implements UpdatesListener {
    private final MessageSender messageSender;
    private final TelegramBot bot;
    private final BotMessages botMessages;
    private final UserService userService;
    private final UserReminderService userReminderService;
    private final UserKeyboardFactory userKeyboardFactory;
    private final AdminKeyboardFactory adminKeyboardFactory;
    private final ConversationService conversationService;
    private final ChallengeAdminService challengeAdminService;
    private final MessageHandlerProvider messageHandlerProvider;

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.callbackQuery() != null) {
                handleCallback(update);
                continue;
            }
            if (update.message() != null) {
                handleMessage(update.message());
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleCallback(Update update) {
        String data = update.callbackQuery().data();
        if (data != null && data.startsWith(UserKeyboardFactory.SET_LOCALE_CALLBACK_PREFIX)) {
            handleSetLocaleCallback(update);
            return;
        }
        if (data != null && data.startsWith(UserKeyboardFactory.DELETE_REMINDER_CALLBACK_PREFIX)) {
            handleDeleteReminderCallback(update);
        }
    }

    private void handleMessage(Message message) {
        su.msk.nlx2.challengebot.model.User appUser = null;
        if (message.from() != null) {
            appUser = userService.syncFromMessage(message);
        }
        if (isPrivateChat(message)) {
            handlePrivateMessage(message, appUser);
        }
    }

    private void handlePrivateMessage(Message message, su.msk.nlx2.challengebot.model.User appUser) {
        Long tgUserId = message.from() != null ? message.from().id() : null;
        if (tgUserId == null || appUser == null) {
            return;
        }
        boolean isAdmin = appUser.getRole() == UserRole.ADMIN;
        Locale locale = resolveLocale(appUser, message.from().languageCode());
        ConversationSession session = conversationService.find(tgUserId).orElse(null);
        if (isCancel(message)) {
            conversationService.clear(tgUserId);
            if (appUser.getMaxPullUps() == null) {
                requestMaxPullUps(message.chat().id(), tgUserId, locale);
            } else {
                messageSender.sendText(message.chat().id(), botMessages.text(locale, "common.action_cancelled"), userKeyboardFactory.mainMenu(locale, isAdmin));
            }
            return;
        }
        if (message.usersShared() != null && isAdmin && session != null && session.getStep() == ConversationStep.ADD_ADMIN_AWAIT_USER) {
            handleSharedUser(message, locale);
            return;
        }
        if (message.chatShared() != null && isAdmin && session != null && session.getStep() == ConversationStep.CREATE_CHALLENGE_AWAIT_CHAT) {
            handleSharedChat(message, session, locale);
            return;
        }
        if (session != null) {
            if (message.text() == null) {
                return;
            }
            messageHandlerProvider.handle(session.getStep(), new MessageHandlerContext(message.chat().id(), tgUserId, message.text().trim(), locale, isAdmin));
            return;
        }
        if (appUser.getMaxPullUps() == null) {
            requestMaxPullUps(message.chat().id(), tgUserId, locale);
            return;
        }
        if (message.text() == null) {
            return;
        }
        String text = message.text().trim();
        if (text.startsWith("/start")) {
            showMainMenu(message.chat().id(), isAdmin, locale);
            return;
        }
        if (handleUserMenuAction(message.chat().id(), tgUserId, text, isAdmin, locale)) {
            return;
        }
        if (isAdmin && handleAdminMenuAction(message.chat().id(), tgUserId, text, locale)) {
            return;
        }
        showMainMenu(message.chat().id(), isAdmin, locale);
    }

    private boolean handleAdminMenuAction(long privateChatId, long tgUserId, String text, Locale locale) {
        if (adminKeyboardFactory.addAdminLabel(locale).equals(text)) {
            conversationService.start(tgUserId, ConversationStep.ADD_ADMIN_AWAIT_USER);
            messageSender.sendText(privateChatId, botMessages.text(locale, "admin.add_admin.select_user"), adminKeyboardFactory.addAdminKeyboard(locale));
            return true;
        }
        if (adminKeyboardFactory.startChallengeLabel(locale).equals(text)) {
            conversationService.start(tgUserId, ConversationStep.CREATE_CHALLENGE_AWAIT_CHAT);
            messageSender.sendText(privateChatId, botMessages.text(locale, "challenge.create.select_chat"), adminKeyboardFactory.selectChatKeyboard(locale));
            return true;
        }
        return false;
    }

    private void handleSharedUser(Message message, Locale locale) {
        SharedUser[] users = message.usersShared().users();
        if (users == null || users.length == 0) {
            messageSender.sendText(message.chat().id(), botMessages.text(locale, "admin.add_admin.user_missing"), adminKeyboardFactory.addAdminKeyboard(locale));
            return;
        }
        var promotedUser = userService.promoteToAdmin(users[0]);
        conversationService.clear(message.from().id());
        messageSender.sendText(message.chat().id(), botMessages.text(locale, "admin.add_admin.success", promotedUser.getName()), userKeyboardFactory.mainMenu(locale, true));
    }

    private void handleSharedChat(Message message, ConversationSession session, Locale locale) {
        if (message.chatShared().requestId() != AdminKeyboardFactory.REQUEST_ID_SELECT_CHAT) {
            messageSender.sendText(message.chat().id(), botMessages.text(locale, "challenge.create.invalid_chat_selection"), adminKeyboardFactory.selectChatKeyboard(locale));
            return;
        }
        if (challengeAdminService.hasActiveOrScheduledProgram(message.chatShared().chatId())) {
            conversationService.clear(message.from().id());
            messageSender.sendText(message.chat().id(), botMessages.text(locale, "challenge.create.already_exists"), userKeyboardFactory.mainMenu(locale, true));
            return;
        }
        session.setChatTgId(message.chatShared().chatId());
        session.setChatTitle(message.chatShared().title() != null ? message.chatShared().title() : "chat " + message.chatShared().chatId());
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_START_DATE);
        messageSender.sendText(message.chat().id(), botMessages.text(locale, "challenge.create.ask_start_date"), adminKeyboardFactory.cancelOnly(locale));
    }

    private boolean handleUserMenuAction(long privateChatId, long tgUserId, String text, boolean isAdmin, Locale locale) {
        if (userKeyboardFactory.setMaxPullUpsLabel(locale).equals(text)) {
            requestMaxPullUps(privateChatId, tgUserId, locale);
            return true;
        }
        if (userKeyboardFactory.addReminderLabel(locale).equals(text)) {
            conversationService.start(tgUserId, ConversationStep.AWAIT_REMINDER_TIME);
            messageSender.sendText(privateChatId, botMessages.text(locale, "user.reminder.ask_time"), adminKeyboardFactory.cancelOnly(locale));
            return true;
        }
        if (userKeyboardFactory.showRemindersLabel(locale).equals(text)) {
            showUserReminders(privateChatId, tgUserId, isAdmin, locale);
            return true;
        }
        if (userKeyboardFactory.changeLanguageLabel(locale).equals(text)) {
            messageSender.sendText(privateChatId, botMessages.text(locale, "locale.choose"), userKeyboardFactory.languageKeyboard(locale));
            return true;
        }
        return false;
    }

    private void requestMaxPullUps(long chatId, long tgUserId, Locale locale) {
        conversationService.start(tgUserId, ConversationStep.AWAIT_MAX_PULL_UPS);
        messageSender.sendText(chatId, botMessages.text(locale, "user.onboarding.max_pull_ups.ask"), adminKeyboardFactory.cancelOnly(locale));
    }

    private void showUserReminders(long chatId, long tgUserId, boolean isAdmin, Locale locale) {
        var reminders = userReminderService.findByTgUserId(tgUserId);
        if (reminders.isEmpty()) {
            messageSender.sendText(chatId, botMessages.text(locale, "user.reminder.list.empty"), userKeyboardFactory.mainMenu(locale, isAdmin));
            return;
        }
        StringBuilder text = new StringBuilder(botMessages.text(locale, "user.reminder.list.title")).append('\n');
        for (int i = 0; i < reminders.size(); i++) {
            text.append(i + 1).append(". ").append(UserKeyboardFactory.formatReminder(reminders.get(i))).append('\n');
        }
        text.append('\n').append(botMessages.text(locale, "user.reminder.list.delete_hint"));
        messageSender.sendText(chatId, text.toString(), userKeyboardFactory.remindersKeyboard(locale, reminders));
    }

    private void handleDeleteReminderCallback(Update update) {
        String data = update.callbackQuery().data();
        Long tgUserId = update.callbackQuery().from().id();
        Long chatId = update.callbackQuery().message().chat().id();
        Locale locale = resolveLocale(userService.findByTgId(tgUserId).orElse(null), update.callbackQuery().from().languageCode());
        Integer reminderId = parseReminderId(data);
        if (tgUserId == null || chatId == null || reminderId == null) {
            bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()).text(botMessages.text(locale, "user.reminder.delete_failed")));
            return;
        }
        boolean deleted = userReminderService.deleteReminder(tgUserId, reminderId);
        boolean isAdmin = userService.isAdmin(tgUserId);
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()).text(deleted ? botMessages.text(locale, "user.reminder.deleted") : botMessages.text(locale, "user.reminder.not_found")));
        showUserReminders(chatId, tgUserId, isAdmin, locale);
    }

    private void handleSetLocaleCallback(Update update) {
        String data = update.callbackQuery().data();
        Long tgUserId = update.callbackQuery().from().id();
        Long chatId = update.callbackQuery().message().chat().id();
        String localeCode = parseLocaleCode(data);
        if (tgUserId == null || chatId == null || localeCode == null) {
            bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()).text("Invalid locale selection."));
            return;
        }
        var user = userService.setLocale(tgUserId, localeCode);
        Locale locale = resolveLocale(user, update.callbackQuery().from().languageCode());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()).text(botMessages.text(locale, "locale.changed")));
        messageSender.sendText(chatId, botMessages.text(locale, "locale.changed"), userKeyboardFactory.mainMenu(locale, isAdmin));
    }

    private void showMainMenu(long chatId, boolean isAdmin, Locale locale) {
        messageSender.sendText(chatId, botMessages.text(locale, "menu.main"), userKeyboardFactory.mainMenu(locale, isAdmin));
    }

    private boolean isPrivateChat(Message message) {
        return message.chat() != null && message.chat().type() == Chat.Type.Private;
    }

    private boolean isCancel(Message message) {
        Locale locale = resolveLocale(userService.findByTgId(message.from().id()).orElse(null), message.from().languageCode());
        return message.text() != null && (adminKeyboardFactory.cancelLabel(locale).equals(message.text().trim()) || "/cancel".equalsIgnoreCase(message.text().trim()));
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

    private Locale resolveLocale(su.msk.nlx2.challengebot.model.User appUser, String telegramLanguageCode) {
        return botMessages.resolveLocale(appUser, telegramLanguageCode);
    }
}
