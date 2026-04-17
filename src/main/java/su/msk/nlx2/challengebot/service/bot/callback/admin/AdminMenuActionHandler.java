package su.msk.nlx2.challengebot.service.bot.callback.admin;

import com.pengrad.telegrambot.model.request.Keyboard;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;

public abstract class AdminMenuActionHandler {
    @Setter
    protected AdminMenuActionHandlerProvider handlerProvider;
    @Setter
    protected ConversationService conversationService;
    @Setter
    protected MessageSender messageSender;
    @Setter
    protected BotMessages botMessages;
    @Setter
    protected AdminKeyboardFactory adminKeyboardFactory;

    @Autowired
    protected void register(AdminMenuActionHandlerProvider adminMenuActionHandlerProvider) {
        adminMenuActionHandlerProvider.register(this);
    }

    protected void startConversation(AdminMenuActionContext context, ConversationStep step, String textKey, Keyboard keyboard) {
        conversationService.start(context.user().getTgId(), step);
        messageSender.sendText(context.privateChatId(), botMessages.text(context.user().getLocale(), textKey), keyboard);
    }

    public abstract String callbackData();

    public abstract void handle(AdminMenuActionContext context);
}
