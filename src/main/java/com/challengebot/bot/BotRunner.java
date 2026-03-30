package com.challengebot.bot;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BotRunner {
    private final TelegramBot bot;
    private final UpdateHandler updateHandler;

    public BotRunner(TelegramBot bot, UpdateHandler updateHandler) {
        this.bot = bot;
        this.updateHandler = updateHandler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        bot.setUpdatesListener(updateHandler);
    }
}
