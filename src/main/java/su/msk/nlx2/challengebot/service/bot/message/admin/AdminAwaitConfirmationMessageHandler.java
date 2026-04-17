package su.msk.nlx2.challengebot.service.bot.message.admin;

import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;
import su.msk.nlx2.challengebot.service.bot.message.MessageParsingUtils;

import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.keyboard.ChallengeKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAwaitConfirmationMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final ChallengeAdminService challengeAdminService;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;
    private final ChallengeKeyboardFactory challengeKeyboardFactory;
    private final UserKeyboardFactory userKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.CREATE_CHALLENGE_AWAIT_CONFIRMATION;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        if (!adminKeyboardFactory.createChallengeLabel(context.locale()).equals(context.text())) {
            messageSender.sendText(
                    context.privateChatId(),
                    botMessages.text(context.locale(), "challenge.create.confirm_repeat"),
                    adminKeyboardFactory.challengeConfirmationKeyboard(context.locale())
            );
            return;
        }
        ConversationSession session = conversationService.find(context.tgUserId()).orElseThrow();
        Program program = challengeAdminService.createProgram(session);
        conversationService.clear(context.tgUserId());
        messageSender.sendText(
                session.getChatTgId(),
                botMessages.text(
                        context.locale(),
                        "challenge.create.chat_created",
                        session.getStartDate(),
                        session.getDaysTotal(),
                        session.getPostTime(),
                        session.getTimezone()
                ),
                challengeKeyboardFactory.joinChallengeKeyboard(context.locale(), program.getId())
        );
        messageSender.sendText(
                context.privateChatId(),
                botMessages.text(context.locale(), "challenge.create.private_created", session.getChatTitle(), program.getId()),
                userKeyboardFactory.mainMenu(context.locale(), true, context.activeParticipant())
        );
    }
}
