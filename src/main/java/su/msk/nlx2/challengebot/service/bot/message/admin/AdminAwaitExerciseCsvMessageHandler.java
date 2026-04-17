package su.msk.nlx2.challengebot.service.bot.message.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.csv.ExerciseCsvImportResult;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.CsvImporter;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.document.TelegramDocumentService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;

@Component
@RequiredArgsConstructor
public class AdminAwaitExerciseCsvMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final CsvImporter csvImporter;
    private final TelegramDocumentService telegramDocumentService;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.AWAIT_EXERCISE_CSV;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        if (!isCsvDocument(context)) {
            messageSender.sendText(
                    context.privateChatId(),
                    botMessages.text(context.locale(), "admin.exercise_csv.invalid_document"),
                    adminKeyboardFactory.cancelOnly(context.locale())
            );
            return;
        }
        Path tempFile = null;
        try {
            tempFile = telegramDocumentService.downloadToTempFile(context.message().document(), "exercise-import-");
            ExerciseCsvImportResult result = csvImporter.importExercises(tempFile);
            conversationService.clear(context.tgUserId());
            messageSender.sendText(
                    context.privateChatId(),
                    botMessages.text(context.locale(), "admin.exercise_csv.success", result.createdCount(), result.updatedCount()),
                    adminKeyboardFactory.adminSectionMenu(context.locale())
            );
        } catch (Exception e) {
            messageSender.sendText(
                    context.privateChatId(),
                    botMessages.text(context.locale(), "admin.exercise_csv.error", resolveErrorMessage(e)),
                    adminKeyboardFactory.cancelOnly(context.locale())
            );
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private boolean isCsvDocument(MessageHandlerContext context) {
        return context.message().document() != null
                && context.message().document().fileName() != null
                && context.message().document().fileName().toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    private String resolveErrorMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }

    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
        }
    }
}
