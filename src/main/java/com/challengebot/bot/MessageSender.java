package com.challengebot.bot;

import com.challengebot.model.Exercise;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.SendResponse;
import java.io.File;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
    private final TelegramBot bot;

    public MessageSender(TelegramBot bot) {
        this.bot = bot;
    }

    public void sendText(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML));
    }

    public void sendHelloButton(long chatId) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
                new InlineKeyboardButton("привет").callbackData("hello_button")
        );
        bot.execute(new SendMessage(chatId, "Нажми кнопку:").replyMarkup(keyboard));
    }

    public String sendVideo(long chatId, Exercise exercise, String caption) {
        // TODO: use cached file_id when available and persist it.
        if (exercise.getFileId() != null && !exercise.getFileId().isBlank()) {
            SendResponse response = bot.execute(new SendVideo(chatId, exercise.getFileId()).caption(caption));
            return response.isOk() ? exercise.getFileId() : null;
        }

        if (exercise.getVideoPath() == null || exercise.getVideoPath().isBlank()) {
            sendText(chatId, caption);
            return null;
        }

        SendResponse response = bot.execute(new SendVideo(chatId, new File(exercise.getVideoPath())).caption(caption));
        if (response.isOk() && response.message() != null && response.message().video() != null) {
            return response.message().video().fileId();
        }
        return null;
    }
}
