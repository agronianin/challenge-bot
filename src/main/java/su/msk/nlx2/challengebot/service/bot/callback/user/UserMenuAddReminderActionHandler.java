package su.msk.nlx2.challengebot.service.bot.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.keyboard.UserKeyboardFactory;
import su.msk.nlx2.challengebot.service.bot.update.PrivateChatUpdateHandler;

@Component
@RequiredArgsConstructor
public class UserMenuAddReminderActionHandler extends UserMenuActionHandler {
    private final PrivateChatUpdateHandler privateChatUpdateHandler;

    @Override
    public String callbackData() {
        return UserKeyboardFactory.USER_MENU_ADD_REMINDER_CALLBACK;
    }

    @Override
    public void handle(UserMenuActionContext context) {
        privateChatUpdateHandler.startAddReminder(context.privateChatId(), context.user());
    }
}
