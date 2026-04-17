package su.msk.nlx2.challengebot.service.bot.message.admin;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.service.BotMessages;

@Component
@RequiredArgsConstructor
public class ChallengeSummaryBuilder {
    private final BotMessages botMessages;

    public String build(ConversationSession session, Locale locale) {
        return botMessages.text(
                locale,
                "challenge.create.summary",
                session.getChatTitle(),
                session.getStartDate(),
                session.getDaysTotal(),
                session.getPostTime(),
                session.getTimezone(),
                session.getExercisesPerDay(),
                session.getTypesPerDay(),
                session.getRestDayFrequency()
        );
    }
}
