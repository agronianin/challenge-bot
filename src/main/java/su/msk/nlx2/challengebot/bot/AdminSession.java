package su.msk.nlx2.challengebot.bot;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSession {
    private AdminFlowStep step;
    private Long chatTgId;
    private String chatTitle;
    private LocalDate startDate;
    private Integer daysTotal;
    private String postTime;
    private String timezone;
    private Integer exercisesPerDay;
    private Integer groupsPerDay;
}
