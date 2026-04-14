package su.msk.nlx2.challengebot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.SendResponse;
import java.io.File;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.model.Exercise;
import su.msk.nlx2.challengebot.model.bot.SentMessageInfo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageSender {
    private final TelegramBot bot;

    public void sendText(long chatId, String text) {
        bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML));
    }

    public void sendText(long chatId, String text, Keyboard keyboard) {
        bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML).replyMarkup(keyboard));
    }

    public SentMessageInfo sendTextWithResult(long chatId, String text) {
        SendResponse response = bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML));
        return toSentMessageInfo(response, null);
    }

    public SentMessageInfo sendTextWithResult(long chatId, String text, Keyboard keyboard) {
        SendResponse response = bot.execute(new SendMessage(chatId, text).parseMode(ParseMode.HTML).replyMarkup(keyboard));
        return toSentMessageInfo(response, null);
    }

    public SentMessageInfo sendVideo(long chatId, Exercise exercise, String caption) {
        if (exercise.getFileId() != null && !exercise.getFileId().isBlank()) {
            SendResponse response = bot.execute(new SendVideo(chatId, exercise.getFileId()).caption(caption).parseMode(ParseMode.HTML));
            if (response.isOk()) {
                return toSentMessageInfo(response, exercise.getFileId());
            }
        }

        if (exercise.getVideoPath() == null || exercise.getVideoPath().isBlank()) {
            return sendTextWithResult(chatId, caption);
        }
        File videoFile = new File(exercise.getVideoPath());
        if (!videoFile.isFile()) {
            return sendTextWithResult(chatId, caption);
        }

        SendResponse response = bot.execute(new SendVideo(chatId, videoFile).caption(caption).parseMode(ParseMode.HTML));
        if (response.isOk() && response.message() != null && response.message().video() != null) {
            return toSentMessageInfo(response, response.message().video().fileId());
        }
        return sendTextWithResult(chatId, caption);
    }

    public boolean deleteMessage(long chatId, int messageId) {
        return bot.execute(new DeleteMessage(chatId, messageId)).isOk();
    }

    private SentMessageInfo toSentMessageInfo(SendResponse response, String fileId) {
        if (!response.isOk() || response.message() == null) {
            return new SentMessageInfo(null, fileId);
        }
        return new SentMessageInfo(response.message().messageId(), fileId);
    }
}
