package su.msk.nlx2.challengebot.bot;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestChat;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestUsers;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public final class AdminKeyboardFactory {
    public static final String ADD_ADMIN = "Добавить администратора";
    public static final String START_CHALLENGE = "Начать challenge";
    public static final String CANCEL = "Отмена";
    public static final String CREATE_CHALLENGE = "Создать challenge";

    public static final int REQUEST_ID_ADD_ADMIN = 1001;
    public static final int REQUEST_ID_SELECT_CHAT = 1002;

    private AdminKeyboardFactory() {
    }

    public static ReplyKeyboardMarkup adminMenu() {
        return new ReplyKeyboardMarkup(
                new String[]{ADD_ADMIN, START_CHALLENGE}
        ).resizeKeyboard(true).isPersistent(true);
    }

    public static ReplyKeyboardMarkup cancelOnly() {
        return new ReplyKeyboardMarkup(new String[]{CANCEL})
                .resizeKeyboard(true)
                .isPersistent(true);
    }

    public static ReplyKeyboardMarkup addAdminKeyboard() {
        KeyboardButtonRequestUsers requestUsers = new KeyboardButtonRequestUsers(REQUEST_ID_ADD_ADMIN)
                .userIsBot(false)
                .maxQuantity(1)
                .requestName(true)
                .requestUsername(true);

        KeyboardButton pickUser = new KeyboardButton("Выбрать пользователя")
                .requestUsers(requestUsers);

        return new ReplyKeyboardMarkup(new KeyboardButton[]{pickUser})
                .addRow(CANCEL)
                .resizeKeyboard(true)
                .isPersistent(true);
    }

    public static ReplyKeyboardMarkup selectChatKeyboard() {
        KeyboardButtonRequestChat requestChat = new KeyboardButtonRequestChat(REQUEST_ID_SELECT_CHAT, false)
                .botIsMember(true)
                .requestTitle(true)
                .requestUsername(true);

        KeyboardButton pickChat = new KeyboardButton("Выбрать чат")
                .requestChat(requestChat);

        return new ReplyKeyboardMarkup(new KeyboardButton[]{pickChat})
                .addRow(CANCEL)
                .resizeKeyboard(true)
                .isPersistent(true);
    }

    public static ReplyKeyboardMarkup challengeConfirmationKeyboard() {
        return new ReplyKeyboardMarkup(
                new String[]{CREATE_CHALLENGE, CANCEL}
        ).resizeKeyboard(true).isPersistent(true);
    }
}
