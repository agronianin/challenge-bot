package su.msk.nlx2.challengebot.service.bot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.context.BotRequestContextResolver;
import su.msk.nlx2.challengebot.service.bot.update.CallbackQueryUpdateHandler;
import su.msk.nlx2.challengebot.service.bot.update.PrivateChatUpdateHandler;

@Component
@RequiredArgsConstructor
public class UpdateHandler implements UpdatesListener {
    private final BotRequestContextResolver botRequestContextResolver;
    private final PrivateChatUpdateHandler privateChatUpdateHandler;
    private final CallbackQueryUpdateHandler callbackQueryUpdateHandler;

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.callbackQuery() != null) {
                callbackQueryUpdateHandler.handle(update.callbackQuery());
                continue;
            }
            if (update.message() != null) {
                handleMessage(update.message());
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleMessage(Message message) {
        var context = botRequestContextResolver.syncFromMessage(message).orElse(null);
        if (context == null || !isPrivateChat(message)) {
            return;
        }
        privateChatUpdateHandler.handle(message, context);
    }

    private boolean isPrivateChat(Message message) {
        return message.chat() != null && message.chat().type() == Chat.Type.Private;
    }
}
