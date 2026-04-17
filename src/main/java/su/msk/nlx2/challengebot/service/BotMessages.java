package su.msk.nlx2.challengebot.service;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.config.LocalizationProperties;
import su.msk.nlx2.challengebot.model.TgUser;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotMessages {
    private static final String DEFAULT_LANGUAGE = "ru";

    private final MessageSource messageSource;
    private final LocalizationProperties localizationProperties;

    public String text(String code, Object... args) {
        return text(defaultLocale(), code, args);
    }

    public String text(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    public String text(TgUser user, String telegramLanguageCode, String code, Object... args) {
        return text(resolveLocale(user, telegramLanguageCode), code, args);
    }

    public Locale resolveLocale(TgUser user, String telegramLanguageCode) {
        if (user != null && user.getLocale() != null) {
            return user.getLocale();
        }
        if (user != null && user.getLocaleCode() != null && !user.getLocaleCode().isBlank()) {
            return toSupportedLocale(user.getLocaleCode());
        }
        if (telegramLanguageCode != null && !telegramLanguageCode.isBlank()) {
            return toSupportedLocale(telegramLanguageCode);
        }
        return defaultLocale();
    }

    public Locale defaultLocale() {
        return toSupportedLocale(localizationProperties.getDefaultLocale());
    }

    private Locale toSupportedLocale(String languageTag) {
        Locale locale = Locale.forLanguageTag(languageTag.replace('_', '-'));
        if (locale.getLanguage().isBlank()) {
            return Locale.forLanguageTag(DEFAULT_LANGUAGE);
        }
        if ("ru".equals(locale.getLanguage()) || "en".equals(locale.getLanguage())) {
            return locale;
        }
        return Locale.forLanguageTag(DEFAULT_LANGUAGE);
    }
}
