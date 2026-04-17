package su.msk.nlx2.challengebot.service.bot.callback.admin;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AdminMenuActionHandlerProvider {
    private final Map<String, AdminMenuActionHandler> handlers = new HashMap<>();

    public void register(AdminMenuActionHandler adminMenuActionHandler) {
        handlers.put(adminMenuActionHandler.callbackData(), adminMenuActionHandler);
        adminMenuActionHandler.setHandlerProvider(this);
    }

    public boolean handles(String callbackData) {
        return handlers.containsKey(callbackData);
    }

    public void handle(String callbackData, AdminMenuActionContext context) {
        AdminMenuActionHandler handler = handlers.get(callbackData);
        if (handler == null) {
            throw new IllegalArgumentException("Handler not found for callbackData=" + callbackData);
        }
        handler.handle(context);
    }
}
