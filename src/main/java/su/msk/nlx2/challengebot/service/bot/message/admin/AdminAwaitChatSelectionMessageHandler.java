package su.msk.nlx2.challengebot.service.bot.message.admin;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;

@Component
@RequiredArgsConstructor
public class AdminAwaitChatSelectionMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ConversationService conversationService;
    private final ChallengeAdminService challengeAdminService;
    private final AdminKeyboardFactory adminKeyboardFactory;
    private final UserKeyboardFactory userKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_CHAT;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        if (context.message().chatShared() == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.repeat_select_chat"), adminKeyboardFactory.selectChatKeyboard(context.locale()));
            return;
        }
        if (context.message().chatShared().requestId() != AdminKeyboardFactory.REQUEST_ID_SELECT_CHAT) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.invalid_chat_selection"), adminKeyboardFactory.selectChatKeyboard(context.locale()));
            return;
        }
        if (challengeAdminService.hasActiveOrScheduledProgram(context.message().chatShared().chatId())) {
            conversationService.clear(context.tgUserId());
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.already_exists"), userKeyboardFactory.mainMenu(context.locale(), true));
            return;
        }
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        session.setChatTgId(context.message().chatShared().chatId());
        session.setChatTitle(context.message().chatShared().title() != null ? context.message().chatShared().title() : "chat " + context.message().chatShared().chatId());
        session.setStartDate(LocalDate.now().plusDays(1));
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_DAYS_TOTAL);
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.start_date_auto", session.getStartDate()), adminKeyboardFactory.cancelOnly(context.locale()));
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.ask_days_total"), adminKeyboardFactory.cancelOnly(context.locale()));
    }
}
