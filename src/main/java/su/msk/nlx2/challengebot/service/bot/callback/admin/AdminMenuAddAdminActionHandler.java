package su.msk.nlx2.challengebot.service.bot.callback.admin;

import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.type.ConversationStep;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;

@Component
public class AdminMenuAddAdminActionHandler extends AdminMenuActionHandler {

    @Override
    public String callbackData() {
        return AdminKeyboardFactory.ADMIN_MENU_ADD_ADMIN_CALLBACK;
    }

    @Override
    public void handle(AdminMenuActionContext context) {
        startConversation(context, ConversationStep.ADD_ADMIN_AWAIT_USER, "admin.add_admin.select_user",
                adminKeyboardFactory.addAdminKeyboard(context.user().getLocale()));
    }
}
