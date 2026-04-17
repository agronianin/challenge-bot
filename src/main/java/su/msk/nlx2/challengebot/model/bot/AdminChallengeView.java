package su.msk.nlx2.challengebot.model.bot;

import java.time.LocalDate;
import su.msk.nlx2.challengebot.model.type.ProgramStatus;

public record AdminChallengeView(
        int id,
        String chatTitle,
        LocalDate startDate,
        String postTime,
        String timezone,
        int daysTotal,
        ProgramStatus status
) {
}
