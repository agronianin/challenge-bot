package su.msk.nlx2.challengebot.service.bot.message;

import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAwaitDaysTotalMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_DAYS_TOTAL;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        Integer daysTotal = MessageParsingUtils.parsePositiveInteger(context.text());
        if (daysTotal == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.invalid_days_total"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        session.setDaysTotal(daysTotal);
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_POST_TIME);
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.ask_post_time"), adminKeyboardFactory.cancelOnly(context.locale()));
    }
}
