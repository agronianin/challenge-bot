package su.msk.nlx2.challengebot.service.bot.callback.user;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class UserMenuActionHandler {

    @Autowired
    protected void register(UserMenuActionHandlerProvider userMenuActionHandlerProvider) {
        userMenuActionHandlerProvider.register(this);
    }

    public abstract String callbackData();

    public abstract void handle(UserMenuActionContext context);
}
