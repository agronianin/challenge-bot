package com.challengebot.bot;

import com.challengebot.config.BotProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.response.GetMeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BotRunner {
    private static final Logger log = LoggerFactory.getLogger(BotRunner.class);

    private final TelegramBot bot;
    private final BotProperties botProperties;
    private final UpdateHandler updateHandler;

    public BotRunner(TelegramBot bot, BotProperties botProperties, UpdateHandler updateHandler) {
        this.bot = bot;
        this.botProperties = botProperties;
        this.updateHandler = updateHandler;
    }

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
