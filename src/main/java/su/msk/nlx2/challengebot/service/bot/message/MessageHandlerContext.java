package su.msk.nlx2.challengebot.service.bot.message;

import java.util.Locale;

public record MessageHandlerContext(
        long privateChatId,
        long tgUserId,
        String text,
        Locale locale,
        boolean admin
) {
}
