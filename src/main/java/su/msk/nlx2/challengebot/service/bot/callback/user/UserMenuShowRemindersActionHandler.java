package su.msk.nlx2.challengebot.service.bot.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.update.PrivateChatUpdateHandler;

@Component
@RequiredArgsConstructor
public class UserMenuShowRemindersActionHandler extends UserMenuActionHandler {
    private final PrivateChatUpdateHandler privateChatUpdateHandler;

    @Override
    public String callbackData() {
        return UserKeyboardFactory.USER_MENU_SHOW_REMINDERS_CALLBACK;
    }

    @Override
    public void handle(UserMenuActionContext context) {
        privateChatUpdateHandler.showUserReminders(context.privateChatId(), context.user());
    }
}
