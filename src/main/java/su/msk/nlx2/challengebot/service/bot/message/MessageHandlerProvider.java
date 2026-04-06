package su.msk.nlx2.challengebot.service.bot.message;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.type.ConversationStep;

@Service
public class MessageHandlerProvider {
    private final Map<ConversationStep, MessageHandler> messageHandlers = new HashMap<>();

    public void register(MessageHandler messageHandler) {
        messageHandlers.put(messageHandler.getStep(), messageHandler);
        messageHandler.setMessageHandlerProvider(this);
    }

    public void handle(ConversationStep flowStep, MessageHandlerContext context) {
        MessageHandler handler = messageHandlers.get(flowStep);
        if (handler == null) {
            throw new IllegalArgumentException("Handler not found for flowStep=" + flowStep.name());
        }
        handler.handle(context);
    }
}
