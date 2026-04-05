package com.challengebot.bot;

import com.challengebot.config.BotProperties;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UpdateHandler implements UpdatesListener {
    private final MessageSender messageSender;
    private final BotProperties botProperties;
    private final TelegramBot bot;

    public UpdateHandler(MessageSender messageSender, BotProperties botProperties, TelegramBot bot) {
        this.messageSender = messageSender;
        this.botProperties = botProperties;
        this.bot = bot;
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.callbackQuery() != null) {
                handleCallback(update);
                continue;
            }
            if (update.message() != null) {
                handleMessage(update.message());
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleCallback(Update update) {
        String data = update.callbackQuery().data();
        Long chatId = update.callbackQuery().message().chat().id();
        if ("hello_button".equals(data)) {
            messageSender.sendText(chatId, "привет");
            bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        }
    }

    private void handleMessage(Message message) {
        if (message.newChatMembers() != null) {
            for (User user : message.newChatMembers()) {
                if (user.username() != null && user.username().equalsIgnoreCase(botProperties.getUsername())) {
                    messageSender.sendHelloButton(message.chat().id());
                    return;
                }
            }
        }

        if (message.text() == null) {
            return;
        }

        String text = message.text().trim();
        if (text.startsWith("/start") || text.startsWith("/hello") || text.startsWith("/привет")) {
            messageSender.sendHelloButton(message.chat().id());
        }
    }
}
