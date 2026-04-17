package su.msk.nlx2.challengebot.service.bot.callback.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.update.PrivateChatUpdateHandler;

@Component
@RequiredArgsConstructor
public class AdminMenuBackToMainActionHandler extends AdminMenuActionHandler {

    private final PrivateChatUpdateHandler privateChatUpdateHandler;

    @Override
    public String callbackData() {
        return AdminKeyboardFactory.ADMIN_MENU_BACK_TO_MAIN_CALLBACK;
    }

    @Override
    public void handle(AdminMenuActionContext context) {
        privateChatUpdateHandler.showMainMenu(context.privateChatId(), context.user());
    }
}
