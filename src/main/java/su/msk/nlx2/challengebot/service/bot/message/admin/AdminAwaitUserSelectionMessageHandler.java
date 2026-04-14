package su.msk.nlx2.challengebot.service.bot.message.admin;

import com.pengrad.telegrambot.model.shared.SharedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.BotMessages;
import su.msk.nlx2.challengebot.service.UserService;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.conversation.ConversationService;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandler;
import su.msk.nlx2.challengebot.service.bot.message.MessageHandlerContext;

@Component
@RequiredArgsConstructor
public class AdminAwaitUserSelectionMessageHandler extends MessageHandler {
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final UserService userService;
    private final ConversationService conversationService;
    private final AdminKeyboardFactory adminKeyboardFactory;
    private final UserKeyboardFactory userKeyboardFactory;

    @Override
    public ConversationStep getStep() {
        return ConversationStep.ADD_ADMIN_AWAIT_USER;
    }

    @Override
    public void handle(MessageHandlerContext context) {
        if (context.message().usersShared() == null) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "admin.add_admin.repeat_select_user"), adminKeyboardFactory.addAdminKeyboard(context.locale()));
            return;
        }
        if (context.message().usersShared().requestId() != AdminKeyboardFactory.REQUEST_ID_ADD_ADMIN) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "admin.add_admin.repeat_select_user"), adminKeyboardFactory.addAdminKeyboard(context.locale()));
            return;
        }
        SharedUser[] users = context.message().usersShared().users();
        if (users == null || users.length == 0) {
            messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "admin.add_admin.user_missing"), adminKeyboardFactory.addAdminKeyboard(context.locale()));
            return;
        }
        var promotedUser = userService.promoteToAdmin(users[0]);
        conversationService.clear(context.tgUserId());
        messageSender.sendText(context.privateChatId(), botMessages.text(context.locale(), "admin.add_admin.success", promotedUser.getName()), userKeyboardFactory.mainMenu(context.locale(), true));
    }
}
