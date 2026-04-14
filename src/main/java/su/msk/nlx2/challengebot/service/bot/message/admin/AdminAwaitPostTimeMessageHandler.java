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
public class AdminAwaitPostTimeMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_POST_TIME;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        if (MessageParsingUtils.parseTime(context.text()) == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "common.invalid_time"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        session.setPostTime(context.text());
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_TIMEZONE);
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.ask_timezone"), adminKeyboardFactory.cancelOnly(context.locale()));
    }
}
