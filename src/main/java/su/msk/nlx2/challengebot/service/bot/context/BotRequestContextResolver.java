package su.msk.nlx2.challengebot.service.bot.context;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.TgUser;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeParticipationService;
import su.msk.nlx2.challengebot.service.UserService;

@Service
@RequiredArgsConstructor
public class BotRequestContextResolver {
    private final UserService userService;
    private final BotMessages botMessages;
    private final ChallengeParticipationService challengeParticipationService;

    public Optional<TgUser> syncFromMessage(Message message) {
        if (message == null || message.from() == null) {
            return Optional.empty();
        }
        TgUser user = userService.syncFromMessage(message);
        return Optional.of(prepareUser(user, message.from().languageCode()));
    }

    public Optional<TgUser> resolve(CallbackQuery callbackQuery) {
        if (callbackQuery == null || callbackQuery.from() == null) {
            return Optional.empty();
        }
        TgUser user = userService.syncFromTelegram(callbackQuery.from());
        return Optional.of(prepareUser(user, callbackQuery.from().languageCode()));
    }

    public boolean isCancel(Message message, String cancelLabel) {
        return message.text() != null
                && (cancelLabel.equals(message.text().trim())
                || "/cancel".equalsIgnoreCase(message.text().trim()));
    }

    private TgUser prepareUser(TgUser user, String telegramLanguageCode) {
        Locale locale = botMessages.resolveLocale(user, telegramLanguageCode);
        user.setLocale(locale);
        user.setActiveParticipant(challengeParticipationService.hasActiveParticipation(user.getTgId()));
        return user;
    }
}
