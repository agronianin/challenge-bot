package su.msk.nlx2.challengebot.service.bot.document;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramDocumentService {
    private final TelegramBot bot;

    public Path downloadToTempFile(Document document, String tempFilePrefix) {
        try {
            byte[] content = download(document);
            Path tempFile = Files.createTempFile(tempFilePrefix, resolveFileSuffix(document.fileName()));
            Files.write(tempFile, content);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить временный файл документа.", e);
        }
    }

    private byte[] download(Document document) {
        if (document == null || document.fileId() == null || document.fileId().isBlank()) {
            throw new IllegalArgumentException("Document fileId is missing.");
        }
        GetFileResponse response = bot.execute(new GetFile(document.fileId()));
        if (!response.isOk() || response.file() == null) {
            throw new IllegalStateException("Не удалось получить файл из Telegram.");
        }
        try {
            return bot.getFileContent(response.file());
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось скачать файл из Telegram.", e);
        }
    }

    private String resolveFileSuffix(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return ".tmp";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".tmp";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
