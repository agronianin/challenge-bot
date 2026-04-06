package su.msk.nlx2.challengebot.service.bot.message;

import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.model.type.UserRole;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAwaitMaxPullUpsMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final UserService userService;
    private final ConversationService conversationService;
    private final UserKeyboardFactory userKeyboardFactory;
    private final AdminKeyboardFactory adminKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.AWAIT_MAX_PULL_UPS;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        Integer maxPullUps = MessageParsingUtils.parseNonNegativeInteger(context.text());
        if (maxPullUps == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "user.onboarding.max_pull_ups.invalid"), adminKeyboardFactory.cancelOnly(context.locale()));
            return;
        }
        var user = userService.setMaxPullUps(context.tgUserId(), maxPullUps);
        conversationService.clear(context.tgUserId());
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "user.onboarding.max_pull_ups.saved", maxPullUps), userKeyboardFactory.mainMenu(context.locale(), isAdmin));
    }
}
