package su.msk.nlx2.challengebot.service.bot.callback.admin;

import static su.msk.nlx2.challengebot.model.type.ConversationStep.CREATE_CHALLENGE_AWAIT_CHAT;

import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;

@Component
public class AdminMenuStartChallengeActionHandler extends AdminMenuActionHandler {

    @Override
    public String callbackData() {
        return AdminKeyboardFactory.ADMIN_MENU_START_CHALLENGE_CALLBACK;
    }

    @Override
    public void handle(AdminMenuActionContext context) {
        startConversation(context, CREATE_CHALLENGE_AWAIT_CHAT, "challenge.create.select_chat",
                adminKeyboardFactory.selectChatKeyboard(context.getLocale()));
    }
}
