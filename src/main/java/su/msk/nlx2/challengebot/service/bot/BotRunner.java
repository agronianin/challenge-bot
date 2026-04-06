package su.msk.nlx2.challengebot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import su.msk.nlx2.challengebot.config.BotProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotRunner {
    private final TelegramBot bot;
    private final BotProperties botProperties;
    private final UpdateHandler updateHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!botProperties.hasConfiguredToken()) {
            log.warn("Telegram polling is disabled: BOT_TOKEN is not configured");
            return;
        }

        GetMeResponse response = bot.execute(new GetMe());
        if (!response.isOk() || response.user() == null) {
            log.warn("Telegram polling is disabled: getMe failed with code {}", response.errorCode());
            return;
        }

        bot.setUpdatesListener(updateHandler);
        log.info("Telegram bot started as @{}", response.user().username());
    }
}
