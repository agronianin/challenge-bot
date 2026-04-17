package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "user_reminder",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_reminders_user_time", columnNames = {"user_id", "remind_time"})
        }
)
public class UserReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private TgUser user;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "remind_time", nullable = false)
    private LocalTime remindTime;
}
