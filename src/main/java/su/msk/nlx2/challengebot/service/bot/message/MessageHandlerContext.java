package su.msk.nlx2.challengebot.service.bot.message;

import com.pengrad.telegrambot.model.Message;
import java.util.Locale;

public record MessageHandlerContext(
        long privateChatId,
        long tgUserId,
        Message message,
        String text,
        Locale locale,
        boolean admin,
        boolean activeParticipant
) {
}
