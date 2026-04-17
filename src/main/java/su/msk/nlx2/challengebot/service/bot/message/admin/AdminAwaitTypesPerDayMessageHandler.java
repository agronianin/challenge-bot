package su.msk.nlx2.challengebot.service.bot.message.admin;

import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;
import su.msk.nlx2.challengebot.service.bot.message.MessageParsingUtils;

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
public class AdminAwaitTypesPerDayMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_TYPES_PER_DAY;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        Integer value = MessageParsingUtils.parsePositiveInteger(context.text());
        if (value == null) {
            messageSender.sendText(
                    context.privateChatId(),
                    botMessages.text(context.locale(), "common.invalid_positive_integer"),
                    adminKeyboardFactory.cancelOnly(context.locale())
            );
            return;
        }
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        session.setTypesPerDay(value);
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_REST_DAY_FREQUENCY);
        messageSender.sendText(
                context.privateChatId(),
                botMessages.text(context.locale(), "challenge.create.ask_rest_day_frequency"),
                adminKeyboardFactory.cancelOnly(context.locale())
        );
    }
}
