package su.msk.nlx2.challengebot.service.bot.callback.admin;

import static su.msk.nlx2.challengebot.model.type.ConversationStep.AWAIT_EXERCISE_CSV;

import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.service.bot.keyboard.AdminKeyboardFactory;

@Component
public class AdminMenuImportExercisesCsvActionHandler extends AdminMenuActionHandler {

    @Override
    public String callbackData() {
        return AdminKeyboardFactory.ADMIN_MENU_IMPORT_EXERCISES_CSV_CALLBACK;
    }

    @Override
    public void handle(AdminMenuActionContext context) {
        startConversation(context, AWAIT_EXERCISE_CSV, "admin.exercise_csv.ask", adminKeyboardFactory.cancelOnly(context.getLocale()));
    }
}
