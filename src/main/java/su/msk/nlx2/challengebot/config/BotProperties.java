package su.msk.nlx2.challengebot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "challenge.bot")
public class BotProperties {
    private String token;
    private String username;
    private String timezone;
    private String postTime;
    private int exercisesPerDay;
    private int groupsPerDay;
    private double repsGrowthPercent;
    private String repsRoundMode;

    public boolean hasConfiguredToken() {
        return token != null
                && !token.isBlank()
                && !"replace_me".equalsIgnoreCase(token.trim());
    }
}
