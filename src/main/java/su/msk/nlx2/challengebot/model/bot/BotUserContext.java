package su.msk.nlx2.challengebot.model.bot;

import java.util.Locale;
import su.msk.nlx2.challengebot.model.User;

public record BotUserContext(
        User user,
        long tgUserId,
        Locale locale,
        boolean admin
) {
}
