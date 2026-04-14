package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "program")
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_tg_id", referencedColumnName = "tg_chat_id", nullable = false)
    private Chat chat;

    @Column(name = "days_total", nullable = false)
    private Integer daysTotal;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "post_time", nullable = false, length = 5)
    private String postTime;

    @Column(name = "timezone", nullable = false, length = 64)
    private String timezone;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "active";

    @Column(name = "exercises_per_day", nullable = false)
    private Integer exercisesPerDay = 3;

    @Column(name = "types_per_day", nullable = false)
    private Integer typesPerDay = 2;

    @Column(name = "rest_day_frequency", nullable = false)
    private Integer restDayFrequency = 0;

    @OneToMany(mappedBy = "program")
    private List<ProgramDay> days = new ArrayList<>();

    @OneToMany(mappedBy = "program")
    private List<ProgramParticipant> participants = new ArrayList<>();
}
