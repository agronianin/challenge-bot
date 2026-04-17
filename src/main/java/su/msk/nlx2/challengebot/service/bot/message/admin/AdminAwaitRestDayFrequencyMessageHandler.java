package su.msk.nlx2.challengebot.service.bot.message.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;
import su.msk.nlx2.challengebot.service.bot.message.MessageParsingUtils;

@Component
@RequiredArgsConstructor
public class AdminAwaitRestDayFrequencyMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;
    private final ChallengeSummaryBuilder challengeSummaryBuilder;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_REST_DAY_FREQUENCY;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        Integer value = MessageParsingUtils.parseNonNegativeInteger(context.text());
        if (value == null) {
            messageSender.sendText(
                    context.privateChatId(),
                    botMessages.text(context.locale(), "common.invalid_non_negative_integer"),
                    adminKeyboardFactory.cancelOnly(context.locale())
            );
            return;
        }
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        session.setRestDayFrequency(value);
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_CONFIRMATION);
        messageSender.sendText(
                context.privateChatId(),
                challengeSummaryBuilder.build(session, context.locale()),
                adminKeyboardFactory.challengeConfirmationKeyboard(context.locale())
        );
    }
}
