package su.msk.nlx2.challengebot.service.bot.callback.admin;

import java.util.Locale;
import su.msk.nlx2.challengebot.model.TgUser;

public record AdminMenuActionContext(long privateChatId, TgUser user) {

    public Locale getLocale() {
        return user.getLocale();
    }
}
