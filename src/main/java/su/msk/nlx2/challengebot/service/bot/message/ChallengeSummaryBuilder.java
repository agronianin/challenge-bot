package su.msk.nlx2.challengebot.service.bot.message;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.service.BotMessages;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeSummaryBuilder {
    private final BotMessages botMessages;

    public String build(ConversationSession session, Locale locale) {
        return botMessages.text(locale, "challenge.create.summary", session.getChatTitle(), session.getStartDate(), session.getDaysTotal(), session.getPostTime(), session.getTimezone(), session.getExercisesPerDay(), session.getGroupsPerDay());
    }
}
