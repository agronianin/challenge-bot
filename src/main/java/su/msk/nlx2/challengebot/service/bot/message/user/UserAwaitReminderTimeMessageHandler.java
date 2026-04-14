package su.msk.nlx2.challengebot.service.bot.message.user;

import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;
import su.msk.nlx2.challengebot.service.bot.message.MessageParsingUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.UserReminderService;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAwaitReminderTimeMessageHandler extends MessageHandler {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final UserReminderService userReminderService;
    private final ConversationService conversationService;
    private final UserKeyboardFactory userKeyboardFactory;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.AWAIT_REMINDER_TIME;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        LocalTime remindTime = MessageParsingUtils.parseTime(context.text());
        if (remindTime == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "common.invalid_time"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        boolean added = userReminderService.addReminder(context.tgUserId(), remindTime);
        if (!added) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "user.reminder.duplicate"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        conversationService.clear(context.tgUserId());
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "user.reminder.added", remindTime.format(TIME_FORMATTER)), userKeyboardFactory.mainMenu(context.locale(), context.admin()));
    }
}
