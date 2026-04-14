package su.msk.nlx2.challengebot.service.bot.keyboard;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.BotMessages;

@Component
@RequiredArgsConstructor
public class ChallengeKeyboardFactory {
    public static final String JOIN_CHALLENGE_CALLBACK_PREFIX = "join_challenge:";

    private final BotMessages botMessages;

    public InlineKeyboardMarkup joinChallengeKeyboard(Locale locale, Integer programId) {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton(botMessages.text(locale, "challenge.join.button"))
                        .callbackData(JOIN_CHALLENGE_CALLBACK_PREFIX + programId)
        );
    }
}
