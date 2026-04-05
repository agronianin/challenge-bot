package su.msk.nlx2.challengebot.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.shared.SharedUser;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.config.BotProperties;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.service.ChallengeAdminService;
import su.msk.nlx2.challengebot.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateHandler implements UpdatesListener {
    private final MessageSender messageSender;
    private final BotProperties botProperties;
    private final TelegramBot bot;
    private final UserService userService;
    private final AdminConversationService adminConversationService;
    private final ChallengeAdminService challengeAdminService;

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.callbackQuery() != null) {
                handleCallback(update);
                continue;
            }
            if (update.message() != null) {
                handleMessage(update.message());
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void handleCallback(Update update) {
        String data = update.callbackQuery().data();
        Long chatId = update.callbackQuery().message().chat().id();
        if ("hello_button".equals(data)) {
            messageSender.sendText(chatId, "привет");
            bot.execute(new AnswerCallbackQuery(update.callbackQuery().id()));
        }
    }

    private void handleMessage(Message message) {
        if (message.from() != null) {
            userService.syncFromMessage(message);
        }

        if (message.newChatMembers() != null) {
            for (User user : message.newChatMembers()) {
                if (user.username() != null && user.username().equalsIgnoreCase(botProperties.getUsername())) {
                    messageSender.sendHelloButton(message.chat().id());
                    return;
                }
            }
        }

        if (isPrivateChat(message)) {
            handlePrivateMessage(message);
            return;
        }

        if (message.text() == null) {
            return;
        }

        String text = message.text().trim();
        if (text.startsWith("/start") || text.startsWith("/hello") || text.startsWith("/привет")) {
            messageSender.sendHelloButton(message.chat().id());
        }
    }

    private void handlePrivateMessage(Message message) {
        Long tgUserId = message.from() != null ? message.from().id() : null;
        if (tgUserId == null) {
            return;
        }

        if (isCancel(message)) {
            adminConversationService.clear(tgUserId);
            if (userService.isAdmin(tgUserId)) {
                showAdminMenu(message.chat().id());
            } else {
                messageSender.sendText(message.chat().id(), "Действие отменено.");
            }
            return;
        }

        if (message.text() != null) {
            String text = message.text().trim();
            if (text.startsWith("/start")) {
                if (userService.isAdmin(tgUserId)) {
                    showAdminMenu(message.chat().id());
                } else {
                    messageSender.sendText(message.chat().id(),
                            "Привет. Сейчас бот ожидает административный доступ. Первый администратор пока добавляется вручную в базу данных.");
                }
                return;
            }
        }

        if (!userService.isAdmin(tgUserId)) {
            return;
        }

        AdminSession session = adminConversationService.find(tgUserId).orElse(null);
        if (message.usersShared() != null && session != null && session.getStep() == AdminFlowStep.ADD_ADMIN_AWAIT_USER) {
            handleSharedUser(message);
            return;
        }

        if (message.chatShared() != null && session != null && session.getStep() == AdminFlowStep.CREATE_CHALLENGE_AWAIT_CHAT) {
            handleSharedChat(message, session);
            return;
        }

        if (message.text() == null) {
            return;
        }

        String text = message.text().trim();
        if (session == null) {
            handleAdminMenuAction(message.chat().id(), tgUserId, text);
            return;
        }

        handleAdminFlowText(message.chat().id(), tgUserId, session, text);
    }

    private void handleAdminMenuAction(long privateChatId, long tgUserId, String text) {
        if (AdminKeyboardFactory.ADD_ADMIN.equals(text)) {
            adminConversationService.start(tgUserId, AdminFlowStep.ADD_ADMIN_AWAIT_USER);
            messageSender.sendText(
                    privateChatId,
                    "Выбери пользователя, которому нужно выдать права администратора.",
                    AdminKeyboardFactory.addAdminKeyboard()
            );
            return;
        }

        if (AdminKeyboardFactory.START_CHALLENGE.equals(text)) {
            adminConversationService.start(tgUserId, AdminFlowStep.CREATE_CHALLENGE_AWAIT_CHAT);
            messageSender.sendText(
                    privateChatId,
                    "Выбери групповой чат, в котором нужно создать challenge.",
                    AdminKeyboardFactory.selectChatKeyboard()
            );
            return;
        }

        showAdminMenu(privateChatId);
    }

    private void handleSharedUser(Message message) {
        SharedUser[] users = message.usersShared().users();
        if (users == null || users.length == 0) {
            messageSender.sendText(message.chat().id(), "Не удалось получить выбранного пользователя.",
                    AdminKeyboardFactory.addAdminKeyboard());
            return;
        }

        var promotedUser = userService.promoteToAdmin(users[0]);
        adminConversationService.clear(message.from().id());
        messageSender.sendText(
                message.chat().id(),
                "Пользователь \"" + promotedUser.getName() + "\" назначен администратором.",
                AdminKeyboardFactory.adminMenu()
        );
    }

    private void handleSharedChat(Message message, AdminSession session) {
        if (message.chatShared().requestId() != AdminKeyboardFactory.REQUEST_ID_SELECT_CHAT) {
            messageSender.sendText(message.chat().id(), "Неверный выбор чата.", AdminKeyboardFactory.selectChatKeyboard());
            return;
        }

        if (challengeAdminService.hasActiveOrScheduledProgram(message.chatShared().chatId())) {
            adminConversationService.clear(message.from().id());
            messageSender.sendText(
                    message.chat().id(),
                    "В этом чате уже есть активный или запланированный challenge.",
                    AdminKeyboardFactory.adminMenu()
            );
            return;
        }

        session.setChatTgId(message.chatShared().chatId());
        session.setChatTitle(message.chatShared().title() != null ? message.chatShared().title() : "chat " + message.chatShared().chatId());
        session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_START_DATE);
        messageSender.sendText(
                message.chat().id(),
                "Введите дату старта в формате YYYY-MM-DD.",
                AdminKeyboardFactory.cancelOnly()
        );
    }

    private void handleAdminFlowText(long privateChatId, long tgUserId, AdminSession session, String text) {
        switch (session.getStep()) {
            case ADD_ADMIN_AWAIT_USER -> messageSender.sendText(
                    privateChatId,
                    "Для добавления администратора выбери пользователя кнопкой ниже.",
                    AdminKeyboardFactory.addAdminKeyboard()
            );
            case CREATE_CHALLENGE_AWAIT_CHAT -> messageSender.sendText(
                    privateChatId,
                    "Для создания challenge сначала выбери чат кнопкой ниже.",
                    AdminKeyboardFactory.selectChatKeyboard()
            );
            case CREATE_CHALLENGE_AWAIT_START_DATE -> handleStartDate(privateChatId, session, text);
            case CREATE_CHALLENGE_AWAIT_DAYS_TOTAL -> handleDaysTotal(privateChatId, session, text);
            case CREATE_CHALLENGE_AWAIT_POST_TIME -> handlePostTime(privateChatId, session, text);
            case CREATE_CHALLENGE_AWAIT_TIMEZONE -> handleTimezone(privateChatId, session, text);
            case CREATE_CHALLENGE_AWAIT_EXERCISES_PER_DAY -> handleExercisesPerDay(privateChatId, session, text);
            case CREATE_CHALLENGE_AWAIT_GROUPS_PER_DAY -> handleGroupsPerDay(privateChatId, session, text);
            case CREATE_CHALLENGE_AWAIT_CONFIRMATION -> handleChallengeConfirmation(privateChatId, tgUserId, session, text);
        }
    }

    private void handleStartDate(long privateChatId, AdminSession session, String text) {
        try {
            LocalDate startDate = LocalDate.parse(text);
            if (startDate.isBefore(LocalDate.now())) {
                messageSender.sendText(privateChatId, "Дата старта не может быть в прошлом.",
                        AdminKeyboardFactory.cancelOnly());
                return;
            }
            session.setStartDate(startDate);
            session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_DAYS_TOTAL);
            messageSender.sendText(privateChatId, "Введите длительность challenge в днях.", AdminKeyboardFactory.cancelOnly());
        } catch (DateTimeParseException e) {
            messageSender.sendText(privateChatId, "Неверный формат даты. Используй YYYY-MM-DD.",
                    AdminKeyboardFactory.cancelOnly());
        }
    }

    private void handleDaysTotal(long privateChatId, AdminSession session, String text) {
        Integer daysTotal = parsePositiveInteger(text);
        if (daysTotal == null) {
            messageSender.sendText(privateChatId, "Введите целое число дней больше 0.",
                    AdminKeyboardFactory.cancelOnly());
            return;
        }
        session.setDaysTotal(daysTotal);
        session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_POST_TIME);
        messageSender.sendText(privateChatId, "Введите время публикации в формате HH:mm.",
                AdminKeyboardFactory.cancelOnly());
    }

    private void handlePostTime(long privateChatId, AdminSession session, String text) {
        if (!text.matches("^(?:[01]\\d|2[0-3]):[0-5]\\d$")) {
            messageSender.sendText(privateChatId, "Неверный формат времени. Используй HH:mm.",
                    AdminKeyboardFactory.cancelOnly());
            return;
        }
        session.setPostTime(text);
        session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_TIMEZONE);
        messageSender.sendText(privateChatId,
                "Введите timezone, например Europe/Moscow.",
                AdminKeyboardFactory.cancelOnly());
    }

    private void handleTimezone(long privateChatId, AdminSession session, String text) {
        try {
            ZoneId.of(text);
            session.setTimezone(text);
            session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_EXERCISES_PER_DAY);
            messageSender.sendText(privateChatId, "Введите количество упражнений в день.",
                    AdminKeyboardFactory.cancelOnly());
        } catch (Exception e) {
            messageSender.sendText(privateChatId,
                    "Неверный timezone. Пример: Europe/Moscow.",
                    AdminKeyboardFactory.cancelOnly());
        }
    }

    private void handleExercisesPerDay(long privateChatId, AdminSession session, String text) {
        Integer value = parsePositiveInteger(text);
        if (value == null) {
            messageSender.sendText(privateChatId, "Введите целое число больше 0.",
                    AdminKeyboardFactory.cancelOnly());
            return;
        }
        session.setExercisesPerDay(value);
        session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_GROUPS_PER_DAY);
        messageSender.sendText(privateChatId, "Введите количество групп мышц в день.",
                AdminKeyboardFactory.cancelOnly());
    }

    private void handleGroupsPerDay(long privateChatId, AdminSession session, String text) {
        Integer value = parsePositiveInteger(text);
        if (value == null) {
            messageSender.sendText(privateChatId, "Введите целое число больше 0.",
                    AdminKeyboardFactory.cancelOnly());
            return;
        }
        session.setGroupsPerDay(value);
        session.setStep(AdminFlowStep.CREATE_CHALLENGE_AWAIT_CONFIRMATION);
        messageSender.sendText(privateChatId, buildChallengeSummary(session), AdminKeyboardFactory.challengeConfirmationKeyboard());
    }

    private void handleChallengeConfirmation(long privateChatId, long tgUserId, AdminSession session, String text) {
        if (!AdminKeyboardFactory.CREATE_CHALLENGE.equals(text)) {
            messageSender.sendText(privateChatId,
                    "Проверь параметры и нажми \"Создать challenge\" или \"Отмена\".",
                    AdminKeyboardFactory.challengeConfirmationKeyboard());
            return;
        }

        Program program = challengeAdminService.createProgram(session);
        adminConversationService.clear(tgUserId);

        messageSender.sendText(
                session.getChatTgId(),
                "Челлендж создан.\n"
                        + "Старт: " + session.getStartDate() + "\n"
                        + "Дней: " + session.getDaysTotal() + "\n"
                        + "Публикация: " + session.getPostTime() + " " + session.getTimezone()
        );

        messageSender.sendText(
                privateChatId,
                "Challenge создан для чата \"" + session.getChatTitle() + "\". ID программы: " + program.getId(),
                AdminKeyboardFactory.adminMenu()
        );
    }

    private void showAdminMenu(long chatId) {
        messageSender.sendText(chatId, "Панель администратора", AdminKeyboardFactory.adminMenu());
    }

    private boolean isPrivateChat(Message message) {
        return message.chat() != null && message.chat().type() == Chat.Type.Private;
    }

    private boolean isCancel(Message message) {
        return message.text() != null
                && (AdminKeyboardFactory.CANCEL.equals(message.text().trim())
                || "/cancel".equalsIgnoreCase(message.text().trim()));
    }

    private Integer parsePositiveInteger(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String buildChallengeSummary(AdminSession session) {
        return "Проверь параметры challenge:\n"
                + "Чат: " + session.getChatTitle() + "\n"
                + "Старт: " + session.getStartDate() + "\n"
                + "Дней: " + session.getDaysTotal() + "\n"
                + "Публикация: " + session.getPostTime() + "\n"
                + "Timezone: " + session.getTimezone() + "\n"
                + "Упражнений в день: " + session.getExercisesPerDay() + "\n"
                + "Групп в день: " + session.getGroupsPerDay();
    }
}
