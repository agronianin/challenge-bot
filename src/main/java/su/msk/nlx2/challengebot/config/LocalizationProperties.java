package su.msk.nlx2.challengebot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "challenge.localization")
public class LocalizationProperties {
    private String defaultLocale = "ru";
}
