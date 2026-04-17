package su.msk.nlx2.challengebot.service.bot.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.update.PrivateChatUpdateHandler;

@Component
@RequiredArgsConstructor
public class UserMenuOpenAdminSectionActionHandler extends UserMenuActionHandler {
    private final PrivateChatUpdateHandler privateChatUpdateHandler;

    @Override
    public String callbackData() {
        return UserKeyboardFactory.USER_MENU_OPEN_ADMIN_SECTION_CALLBACK;
    }

    @Override
    public void handle(UserMenuActionContext context) {
        if (!context.user().isAdmin()) {
            privateChatUpdateHandler.showMainMenu(context.privateChatId(), context.user());
            return;
        }
        privateChatUpdateHandler.showAdminSectionMenu(context.privateChatId(), context.user().getLocale());
    }
}
