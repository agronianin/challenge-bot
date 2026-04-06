package su.msk.nlx2.challengebot.service.bot.context;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.bot.BotUserContext;
import su.msk.nlx2.challengebot.model.type.UserRole;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.UserService;

@Service
@RequiredArgsConstructor
public class BotRequestContextResolver {
    private final UserService userService;
    private final BotMessages botMessages;

    public Optional<BotUserContext> syncFromMessage(Message message) {
        if (message == null || message.from() == null) {
            return Optional.empty();
        }
        var user = userService.syncFromMessage(message);
        return Optional.of(toContext(user, message.from().id(), message.from().languageCode()));
    }

    public Optional<BotUserContext> resolve(CallbackQuery callbackQuery) {
        if (callbackQuery == null || callbackQuery.from() == null) {
            return Optional.empty();
        }
        Long tgUserId = callbackQuery.from().id();
        return userService.findByTgId(tgUserId)
                .map(user -> toContext(user, tgUserId, callbackQuery.from().languageCode()));
    }

    public boolean isCancel(Message message, String cancelLabel) {
        return message.text() != null
                && (cancelLabel.equals(message.text().trim())
                || "/cancel".equalsIgnoreCase(message.text().trim()));
    }

    private BotUserContext toContext(su.msk.nlx2.challengebot.model.User user, long tgUserId, String telegramLanguageCode) {
        return new BotUserContext(
                user,
                tgUserId,
                botMessages.resolveLocale(user, telegramLanguageCode),
                user.getRole() == UserRole.ADMIN
        );
    }
}
