package su.msk.nlx2.challengebot.service.bot.message;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAwaitStartDateMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_START_DATE;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        LocalDate startDate = MessageParsingUtils.parseDate(context.text());
        if (startDate == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.invalid_start_date"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        if (startDate.isBefore(LocalDate.now())) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.start_date_past"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        session.setStartDate(startDate);
        session.setStep(ConversationStep.CREATE_CHALLENGE_AWAIT_DAYS_TOTAL);
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "challenge.create.ask_days_total"), adminKeyboardFactory.cancelOnly(context.locale()));
    }
}
