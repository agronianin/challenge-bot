package su.msk.nlx2.challengebot.service.bot.keyboard;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestChat;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestUsers;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminKeyboardFactory {

    public static final String ACTION_ADD_ADMIN = "add_admin";
    public static final String ACTION_START_CHALLENGE = "start_challenge";
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_CREATE_CHALLENGE = "create_challenge";

    public static final int REQUEST_ID_ADD_ADMIN = 1001;
    public static final int REQUEST_ID_SELECT_CHAT = 1002;

    private final BotMessages botMessages;

    public ReplyKeyboardMarkup adminMenu(Locale locale) {
        return new ReplyKeyboardMarkup(addAdminLabel(locale), startChallengeLabel(locale)).resizeKeyboard(true).isPersistent(true);
    }

    public ReplyKeyboardMarkup cancelOnly(Locale locale) {
        return new ReplyKeyboardMarkup(cancelLabel(locale)).resizeKeyboard(true).isPersistent(true);
    }

    public ReplyKeyboardMarkup addAdminKeyboard(Locale locale) {
        KeyboardButtonRequestUsers requestUsers = new KeyboardButtonRequestUsers(REQUEST_ID_ADD_ADMIN)
                .userIsBot(false)
                .maxQuantity(1)
                .requestName(true)
                .requestUsername(true);
        KeyboardButton pickUser = new KeyboardButton(botMessages.text(locale, "menu.admin.pick_user")).requestUsers(requestUsers);
        return new ReplyKeyboardMarkup(pickUser).addRow(cancelLabel(locale)).resizeKeyboard(true).isPersistent(true);
    }

    public ReplyKeyboardMarkup selectChatKeyboard(Locale locale) {
        KeyboardButtonRequestChat requestChat = new KeyboardButtonRequestChat(REQUEST_ID_SELECT_CHAT, false)
                .botIsMember(true)
                .requestTitle(true)
                .requestUsername(true);
        KeyboardButton pickChat = new KeyboardButton(botMessages.text(locale, "menu.admin.pick_chat")).requestChat(requestChat);
        return new ReplyKeyboardMarkup(pickChat).addRow(cancelLabel(locale)).resizeKeyboard(true).isPersistent(true);
    }

    public ReplyKeyboardMarkup challengeConfirmationKeyboard(Locale locale) {
        return new ReplyKeyboardMarkup(createChallengeLabel(locale), cancelLabel(locale)).resizeKeyboard(true).isPersistent(true);
    }

    public String addAdminLabel(Locale locale) {
        return botMessages.text(locale, "menu.admin.add_admin");
    }

    public String startChallengeLabel(Locale locale) {
        return botMessages.text(locale, "menu.admin.start_challenge");
    }

    public String cancelLabel(Locale locale) {
        return botMessages.text(locale, "common.cancel");
    }

    public String createChallengeLabel(Locale locale) {
        return botMessages.text(locale, "challenge.create");
    }
}
