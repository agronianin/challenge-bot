package su.msk.nlx2.challengebot.service.bot.update;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.TgUser;
import su.msk.nlx2.challengebot.model.UserReminder;
import su.msk.nlx2.challengebot.model.bot.AdminChallengeView;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import su.msk.nlx2.challengebot.service.ChallengeParticipationService;
import su.msk.nlx2.challengebot.service.UserReminderService;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.context.BotRequestContextResolver;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerProvider;

@Service
@RequiredArgsConstructor
public class PrivateChatUpdateHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final UserReminderService userReminderService;
    private final UserKeyboardFactory userKeyboardFactory;
    private final AdminKeyboardFactory adminKeyboardFactory;
    private final ConversationService conversationService;
    private final ChallengeAdminService challengeAdminService;
    private final ChallengeParticipationService challengeParticipationService;
    private final MessageHandlerProvider messageHandlerProvider;
    private final BotRequestContextResolver botRequestContextResolver;

    public void handle(Message message, TgUser user) {
        long privateChatId = message.chat().id();
        ConversationSession session = conversationService.find(user.getTgId()).orElse(null);
        if (botRequestContextResolver.isCancel(message, adminKeyboardFactory.cancelLabel(user.getLocale()))) {
            handleCancel(privateChatId, user);
            return;
        }
        if (session != null) {
            handleConversationMessage(message, user, session);
            return;
        }
        if (user.getMaxPullUps() == null) {
            requestMaxPullUps(privateChatId, user.getTgId(), user.getLocale());
            return;
        }
        if (message.text() == null) {
            return;
        }
        String text = message.text().trim();
        if (text.startsWith("/start")) {
            showMainMenu(privateChatId, user);
            return;
        }
        showMainMenu(privateChatId, user);
    }

    public void showUserReminders(long chatId, TgUser user) {
        List<UserReminder> reminders = userReminderService.findByTgUserId(user.getTgId());
        if (reminders.isEmpty()) {
            messageSender.sendText(chatId, botMessages.text(user.getLocale(), "user.reminder.list.empty"), mainMenuKeyboard(user));
            return;
        }
        StringBuilder text = new StringBuilder(botMessages.text(user.getLocale(), "user.reminder.list.title")).append('\n');
        for (int i = 0; i < reminders.size(); i++) {
            text.append(i + 1).append(". ").append(UserKeyboardFactory.formatReminder(reminders.get(i))).append('\n');
        }
        text.append('\n').append(botMessages.text(user.getLocale(), "user.reminder.list.delete_hint"));
        messageSender.sendText(chatId, text.toString(), userKeyboardFactory.remindersKeyboard(user.getLocale(), reminders));
    }

    public void showMainMenu(long chatId, TgUser user) {
        messageSender.sendText(chatId, botMessages.text(user.getLocale(), "menu.main"), mainMenuKeyboard(user));
    }

    public void startSetMaxPullUps(long chatId, TgUser user) {
        requestMaxPullUps(chatId, user.getTgId(), user.getLocale());
    }

    public void startAddReminder(long chatId, TgUser user) {
        conversationService.start(user.getTgId(), ConversationStep.AWAIT_REMINDER_TIME);
        messageSender.sendText(
                chatId,
                botMessages.text(user.getLocale(), "user.reminder.ask_time"),
                adminKeyboardFactory.cancelOnly(user.getLocale())
        );
    }

    public void showLocaleSelection(long chatId, TgUser user) {
        messageSender.sendText(
                chatId,
                botMessages.text(user.getLocale(), "locale.choose"),
                userKeyboardFactory.languageKeyboard(user.getLocale())
        );
    }

    public void giveUp(long chatId, TgUser user) {
        int count = challengeParticipationService.giveUpActiveParticipations(user.getTgId());
        user.setActiveParticipant(false);
        String key = count > 0 ? "user.participation.give_up.success" : "user.participation.give_up.not_found";
        messageSender.sendText(chatId, botMessages.text(user.getLocale(), key), mainMenuKeyboard(user));
    }

    public void showAdminSectionMenu(long chatId, Locale locale) {
        messageSender.sendText(chatId, botMessages.text(locale, "menu.admin.section"), adminKeyboardFactory.adminSectionMenu(locale));
    }

    public void showDeleteChallengeConfirmation(long chatId, Locale locale, int challengeId) {
        messageSender.sendText(
                chatId,
                botMessages.text(locale, "admin.challenge.delete.confirm", challengeId),
                adminKeyboardFactory.confirmDeleteChallengeKeyboard(locale, challengeId)
        );
    }

    public void showManageChallenges(long chatId, Locale locale) {
        List<AdminChallengeView> challenges = challengeAdminService.findManageablePrograms();
        if (challenges.isEmpty()) {
            messageSender.sendText(chatId, botMessages.text(locale, "admin.challenge.list.empty"));
            return;
        }
        messageSender.sendText(
                chatId,
                buildManageChallengesText(challenges, locale),
                adminKeyboardFactory.manageChallengesKeyboard(locale, challenges)
        );
    }

    private String buildManageChallengesText(List<AdminChallengeView> challenges, Locale locale) {
        StringBuilder text = new StringBuilder(botMessages.text(locale, "admin.challenge.list.title")).append('\n').append('\n');
        for (AdminChallengeView challenge : challenges) {
            text.append('#').append(challenge.id()).append(" | ")
                    .append(challenge.chatTitle()).append('\n')
                    .append(botMessages.text(
                            locale,
                            "admin.challenge.item_status",
                            botMessages.text(locale, "admin.challenge.status." + challenge.status().name().toLowerCase())
                    )).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_start", challenge.startDate())).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_post_time", challenge.postTime(), challenge.timezone())).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_days_total", challenge.daysTotal())).append('\n')
                    .append('\n');
        }
        return text.toString().trim();
    }

    private void handleCancel(long chatId, TgUser user) {
        conversationService.clear(user.getTgId());
        if (user.getMaxPullUps() == null) {
            requestMaxPullUps(chatId, user.getTgId(), user.getLocale());
            return;
        }
        messageSender.sendText(chatId, botMessages.text(user.getLocale(), "common.action_cancelled"), mainMenuKeyboard(user));
    }

    private void handleConversationMessage(Message message, TgUser user, ConversationSession session) {
        String text = message.text() == null ? null : message.text().trim();
        MessageHandlerContext handlerContext = new MessageHandlerContext(
                message.chat().id(),
                user.getTgId(),
                message,
                text,
                user.getLocale(),
                user.isAdmin(),
                user.isActiveParticipant()
        );
        messageHandlerProvider.handle(session.getStep(), handlerContext);
    }

    private void requestMaxPullUps(long chatId, long tgUserId, Locale locale) {
        conversationService.start(tgUserId, ConversationStep.AWAIT_MAX_PULL_UPS);
        messageSender.sendText(chatId, botMessages.text(locale, "user.onboarding.max_pull_ups.ask"), adminKeyboardFactory.cancelOnly(locale));
    }

    private Keyboard mainMenuKeyboard(TgUser user) {
        return userKeyboardFactory.mainMenu(user.getLocale(), user.isAdmin(), user.isActiveParticipant());
    }
}
