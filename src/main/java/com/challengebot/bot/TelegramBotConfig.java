package com.challengebot.bot;

import com.challengebot.config.BotProperties;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfig {
    @Bean
    public TelegramBot telegramBot(BotProperties properties) {
        return new TelegramBot(properties.getToken());
    }
}
