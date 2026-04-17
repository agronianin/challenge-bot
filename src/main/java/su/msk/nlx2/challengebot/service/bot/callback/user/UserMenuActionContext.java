package su.msk.nlx2.challengebot.service.bot.callback.user;

import su.msk.nlx2.challengebot.model.TgUser;

public record UserMenuActionContext(long privateChatId, TgUser user) {
}
