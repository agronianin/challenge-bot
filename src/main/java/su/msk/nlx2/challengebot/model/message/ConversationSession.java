package su.msk.nlx2.challengebot.model.message;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import su.msk.nlx2.challengebot.model.type.ConversationStep;

@Getter
@Setter
public class ConversationSession {
    private ConversationStep step;
    private Long chatTgId;
    private String chatTitle;
    private LocalDate startDate;
    private Integer daysTotal;
    private String postTime;
    private String timezone;
    private Integer exercisesPerDay;
    private Integer groupsPerDay;
}
