package su.msk.nlx2.challengebot.service.bot.update;

import com.pengrad.telegrambot.model.Message;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.bot.AdminChallengeView;
import su.msk.nlx2.challengebot.model.bot.BotUserContext;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
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
    private final MessageHandlerProvider messageHandlerProvider;
    private final BotRequestContextResolver botRequestContextResolver;

    public void handle(Message message, BotUserContext context) {
        ConversationSession session = conversationService.find(context.tgUserId()).orElse(null);
        if (botRequestContextResolver.isCancel(message, adminKeyboardFactory.cancelLabel(context.locale()))) {
            handleCancel(message.chat().id(), context);
            return;
        }
        if (session != null) {
            handleConversationMessage(message, context, session);
            return;
        }
        if (context.user().getMaxPullUps() == null) {
            requestMaxPullUps(message.chat().id(), context.tgUserId(), context.locale());
            return;
        }
        if (message.text() == null) {
            return;
        }
        String text = message.text().trim();
        if (text.startsWith("/start")) {
            showMainMenu(message.chat().id(), context.admin(), context.locale());
            return;
        }
        if (handleUserMenuAction(message.chat().id(), context, text)) {
            return;
        }
        if (context.admin() && handleAdminMenuAction(message.chat().id(), context, text)) {
            return;
        }
        showMainMenu(message.chat().id(), context.admin(), context.locale());
    }

    public void showUserReminders(long chatId, BotUserContext context) {
        var reminders = userReminderService.findByTgUserId(context.tgUserId());
        if (reminders.isEmpty()) {
            messageSender.sendText(chatId, botMessages.text(context.locale(), "user.reminder.list.empty"), userKeyboardFactory.mainMenu(context.locale(), context.admin()));
            return;
        }
        StringBuilder text = new StringBuilder(botMessages.text(context.locale(), "user.reminder.list.title")).append('\n');
        for (int i = 0; i < reminders.size(); i++) {
            text.append(i + 1).append(". ").append(UserKeyboardFactory.formatReminder(reminders.get(i))).append('\n');
        }
        text.append('\n').append(botMessages.text(context.locale(), "user.reminder.list.delete_hint"));
        messageSender.sendText(chatId, text.toString(), userKeyboardFactory.remindersKeyboard(context.locale(), reminders));
    }

    public void showMainMenu(long chatId, boolean isAdmin, Locale locale) {
        messageSender.sendText(chatId, botMessages.text(locale, "menu.main"), userKeyboardFactory.mainMenu(locale, isAdmin));
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
        var challenges = challengeAdminService.findManageablePrograms();
        if (challenges.isEmpty()) {
            messageSender.sendText(chatId, botMessages.text(locale, "admin.challenge.list.empty"));
            return;
        }
        StringBuilder text = new StringBuilder(botMessages.text(locale, "admin.challenge.list.title")).append('\n').append('\n');
        for (AdminChallengeView challenge : challenges) {
            text.append('#').append(challenge.id()).append(" | ")
                    .append(challenge.chatTitle()).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_status", botMessages.text(locale, "admin.challenge.status." + challenge.status()))).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_start", challenge.startDate())).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_post_time", challenge.postTime(), challenge.timezone())).append('\n')
                    .append(botMessages.text(locale, "admin.challenge.item_days_total", challenge.daysTotal())).append('\n')
                    .append('\n');
        }
        messageSender.sendText(chatId, text.toString().trim(), adminKeyboardFactory.manageChallengesKeyboard(locale, challenges));
    }

    private void handleCancel(long chatId, BotUserContext context) {
        conversationService.clear(context.tgUserId());
        if (context.user().getMaxPullUps() == null) {
            requestMaxPullUps(chatId, context.tgUserId(), context.locale());
            return;
        }
        messageSender.sendText(chatId, botMessages.text(context.locale(), "common.action_cancelled"), userKeyboardFactory.mainMenu(context.locale(), context.admin()));
    }

    private void handleConversationMessage(Message message, BotUserContext context, ConversationSession session) {
        String text = message.text() == null ? null : message.text().trim();
        messageHandlerProvider.handle(session.getStep(), new MessageHandlerContext(message.chat().id(), context.tgUserId(), message, text, context.locale(), context.admin()));
    }

    private boolean handleUserMenuAction(long privateChatId, BotUserContext context, String text) {
        if (userKeyboardFactory.setMaxPullUpsLabel(context.locale()).equals(text)) {
            requestMaxPullUps(privateChatId, context.tgUserId(), context.locale());
            return true;
        }
        if (userKeyboardFactory.addReminderLabel(context.locale()).equals(text)) {
            conversationService.start(context.tgUserId(), ConversationStep.AWAIT_REMINDER_TIME);
            messageSender.sendText(privateChatId, botMessages.text(context.locale(), "user.reminder.ask_time"), adminKeyboardFactory.cancelOnly(context.locale()));
            return true;
        }
        if (userKeyboardFactory.showRemindersLabel(context.locale()).equals(text)) {
            showUserReminders(privateChatId, context);
            return true;
        }
        if (userKeyboardFactory.changeLanguageLabel(context.locale()).equals(text)) {
            messageSender.sendText(privateChatId, botMessages.text(context.locale(), "locale.choose"), userKeyboardFactory.languageKeyboard(context.locale()));
            return true;
        }
        return false;
    }

    private boolean handleAdminMenuAction(long privateChatId, BotUserContext context, String text) {
        if (adminKeyboardFactory.adminSectionLabel(context.locale()).equals(text)) {
            showAdminSectionMenu(privateChatId, context.locale());
            return true;
        }
        if (adminKeyboardFactory.backToMainMenuLabel(context.locale()).equals(text)) {
            showMainMenu(privateChatId, true, context.locale());
            return true;
        }
        if (adminKeyboardFactory.addAdminLabel(context.locale()).equals(text)) {
            conversationService.start(context.tgUserId(), ConversationStep.ADD_ADMIN_AWAIT_USER);
            messageSender.sendText(privateChatId, botMessages.text(context.locale(), "admin.add_admin.select_user"), adminKeyboardFactory.addAdminKeyboard(context.locale()));
            return true;
        }
        if (adminKeyboardFactory.startChallengeLabel(context.locale()).equals(text)) {
            conversationService.start(context.tgUserId(), ConversationStep.CREATE_CHALLENGE_AWAIT_CHAT);
            messageSender.sendText(privateChatId, botMessages.text(context.locale(), "challenge.create.select_chat"), adminKeyboardFactory.selectChatKeyboard(context.locale()));
            return true;
        }
        if (adminKeyboardFactory.manageChallengesLabel(context.locale()).equals(text)) {
            showManageChallenges(privateChatId, context.locale());
            return true;
        }
        if (adminKeyboardFactory.importExercisesCsvLabel(context.locale()).equals(text)) {
            conversationService.start(context.tgUserId(), ConversationStep.AWAIT_EXERCISE_CSV);
            messageSender.sendText(privateChatId, botMessages.text(context.locale(), "admin.exercise_csv.ask"), adminKeyboardFactory.cancelOnly(context.locale()));
            return true;
        }
        return false;
    }

    private void requestMaxPullUps(long chatId, long tgUserId, Locale locale) {
        conversationService.start(tgUserId, ConversationStep.AWAIT_MAX_PULL_UPS);
        messageSender.sendText(chatId, botMessages.text(locale, "user.onboarding.max_pull_ups.ask"), adminKeyboardFactory.cancelOnly(locale));
    }

}
