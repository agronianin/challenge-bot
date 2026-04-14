package su.msk.nlx2.challengebot.service.bot.message;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import su.msk.nlx2.challengebot.model.type.ConversationStep;

public abstract class MessageHandler {

    @Setter
    protected MessageHandlerProvider messageHandlerProvider;

    @Autowired
    protected void register(MessageHandlerProvider messageHandlerProvider) {
        messageHandlerProvider.register(this);
    }

    public abstract ConversationStep getStep();

    public abstract void handle(MessageHandlerContext context);
}
