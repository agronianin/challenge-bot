package su.msk.nlx2.challengebot.service.bot.message;

import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAwaitChatSelectionMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_CHAT;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.repeat_select_chat"), adminKeyboardFactory.selectChatKeyboard(context.locale()));
    }
}
