package su.msk.nlx2.challengebot.service.bot.keyboard;

import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestChat;
import com.pengrad.telegrambot.model.request.KeyboardButtonRequestUsers;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.model.bot.AdminChallengeView;
import su.msk.nlx2.challengebot.model.type.ProgramStatus;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminKeyboardFactory {
    public static final int REQUEST_ID_ADD_ADMIN = 1001;
    public static final int REQUEST_ID_SELECT_CHAT = 1002;
    public static final String ADMIN_MENU_ADD_ADMIN_CALLBACK = "admin_menu:add_admin";
    public static final String ADMIN_MENU_START_CHALLENGE_CALLBACK = "admin_menu:start_challenge";
    public static final String ADMIN_MENU_MANAGE_CHALLENGES_CALLBACK = "admin_menu:manage_challenges";
    public static final String ADMIN_MENU_IMPORT_EXERCISES_CSV_CALLBACK = "admin_menu:import_exercises_csv";
    public static final String ADMIN_MENU_BACK_TO_MAIN_CALLBACK = "admin_menu:back_to_main";
    public static final String CHALLENGE_PAUSE_CALLBACK_PREFIX = "challenge_pause:";
    public static final String CHALLENGE_DELETE_CALLBACK_PREFIX = "challenge_delete:";
    public static final String CHALLENGE_DELETE_CONFIRM_CALLBACK_PREFIX = "challenge_delete_confirm:";
    public static final String CHALLENGE_DELETE_CANCEL_CALLBACK_PREFIX = "challenge_delete_cancel:";
    public static final String CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX = "challenge_republish_last_day:";
    private static final String[] TIMEZONE_ROW_1 = {"Europe/Moscow", "Europe/Kaliningrad"};
    private static final String[] TIMEZONE_ROW_2 = {"Asia/Yekaterinburg", "Asia/Novosibirsk"};
    private static final String[] TIMEZONE_ROW_3 = {"Asia/Krasnoyarsk", "Asia/Irkutsk"};
    private static final String[] TIMEZONE_ROW_4 = {"Asia/Yakutsk", "Asia/Vladivostok"};
    private static final String[] TIMEZONE_ROW_5 = {"Asia/Magadan", "Asia/Kamchatka"};
    private static final String[] TIMEZONE_ROW_6 = {"UTC", "Europe/Berlin"};

    private final BotMessages botMessages;

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

    public ReplyKeyboardMarkup timezoneKeyboard(Locale locale) {
        return new ReplyKeyboardMarkup(TIMEZONE_ROW_1)
                .addRow(TIMEZONE_ROW_2)
                .addRow(TIMEZONE_ROW_3)
                .addRow(TIMEZONE_ROW_4)
                .addRow(TIMEZONE_ROW_5)
                .addRow(TIMEZONE_ROW_6)
                .addRow(cancelLabel(locale))
                .resizeKeyboard(true)
                .isPersistent(true);
    }

    public InlineKeyboardMarkup adminSectionMenu(Locale locale) {
        return new InlineKeyboardMarkup()
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "menu.admin.add_admin"))
                                .callbackData(ADMIN_MENU_ADD_ADMIN_CALLBACK),
                        new InlineKeyboardButton(botMessages.text(locale, "menu.admin.start_challenge"))
                                .callbackData(ADMIN_MENU_START_CHALLENGE_CALLBACK)
                )
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "menu.admin.manage_challenges"))
                                .callbackData(ADMIN_MENU_MANAGE_CHALLENGES_CALLBACK),
                        new InlineKeyboardButton(botMessages.text(locale, "menu.admin.import_exercises_csv"))
                                .callbackData(ADMIN_MENU_IMPORT_EXERCISES_CSV_CALLBACK)
                )
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "menu.admin.back_to_main"))
                                .callbackData(ADMIN_MENU_BACK_TO_MAIN_CALLBACK)
                );
    }

    public InlineKeyboardMarkup manageChallengesKeyboard(Locale locale, java.util.List<AdminChallengeView> challenges) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        for (AdminChallengeView challenge : challenges) {
            if (challenge.status() != ProgramStatus.PAUSED) {
                markup.addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.pause_button", challenge.id()))
                                .callbackData(CHALLENGE_PAUSE_CALLBACK_PREFIX + challenge.id()),
                        new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.delete_button", challenge.id()))
                                .callbackData(CHALLENGE_DELETE_CALLBACK_PREFIX + challenge.id())
                );
                markup.addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.republish_last_day_button", challenge.id()))
                                .callbackData(CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX + challenge.id())
                );
                continue;
            }
            markup.addRow(
                    new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.delete_button", challenge.id()))
                            .callbackData(CHALLENGE_DELETE_CALLBACK_PREFIX + challenge.id())
            );
            markup.addRow(
                    new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.republish_last_day_button", challenge.id()))
                            .callbackData(CHALLENGE_REPUBLISH_LAST_DAY_CALLBACK_PREFIX + challenge.id())
            );
        }
        return markup;
    }

    public InlineKeyboardMarkup confirmDeleteChallengeKeyboard(Locale locale, int challengeId) {
        return new InlineKeyboardMarkup()
                .addRow(
                        new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.delete_confirm_yes"))
                                .callbackData(CHALLENGE_DELETE_CONFIRM_CALLBACK_PREFIX + challengeId),
                        new InlineKeyboardButton(botMessages.text(locale, "admin.challenge.delete_confirm_no"))
                                .callbackData(CHALLENGE_DELETE_CANCEL_CALLBACK_PREFIX + challengeId)
                );
    }

    public String cancelLabel(Locale locale) {
        return botMessages.text(locale, "common.cancel");
    }

    public String createChallengeLabel(Locale locale) {
        return botMessages.text(locale, "challenge.create");
    }
}
