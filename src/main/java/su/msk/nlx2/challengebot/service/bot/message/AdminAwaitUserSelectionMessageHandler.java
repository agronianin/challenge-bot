package su.msk.nlx2.challengebot.service.bot.message;

import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAwaitUserSelectionMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.ADD_ADMIN_AWAIT_USER;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "admin.add_admin.repeat_select_user"), adminKeyboardFactory.addAdminKeyboard(context.locale()));
    }
}
