package su.msk.nlx2.challengebot.service.bot.callback.user;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class UserMenuActionHandlerProvider {
    private final Map<String, UserMenuActionHandler> handlers = new HashMap<>();

    public void register(UserMenuActionHandler userMenuActionHandler) {
        handlers.put(userMenuActionHandler.callbackData(), userMenuActionHandler);
        userMenuActionHandler.setHandlerProvider(this);
    }

    public boolean handles(String callbackData) {
        return handlers.containsKey(callbackData);
    }

    public void handle(String callbackData, UserMenuActionContext context) {
        UserMenuActionHandler handler = handlers.get(callbackData);
        if (handler == null) {
            throw new IllegalArgumentException("Handler not found for callbackData=" + callbackData);
        }
        handler.handle(context);
    }
}
