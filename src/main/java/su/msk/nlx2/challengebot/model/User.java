package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.msk.nlx2.challengebot.model.type.UserRole;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tg_id", nullable = false, unique = true)
    private Long tgId;

    @Column(name = "name")
    private String name;

    @Column(name = "locale", length = 16)
    private String locale;

    @Column(name = "max_pull_ups")
    private Integer maxPullUps;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @OneToMany(mappedBy = "user")
    private List<Completion> completions = new ArrayList<>();

    @OrderBy("remindTime asc")
    @OneToMany(mappedBy = "user")
    private List<UserReminder> reminders = new ArrayList<>();
}
