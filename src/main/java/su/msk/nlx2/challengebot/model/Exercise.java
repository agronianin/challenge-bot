package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import su.msk.nlx2.challengebot.model.type.RepsUnit;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "exercise",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_exercise_name", columnNames = "name")
        }
)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "exercise_type_link",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "type_name")
    )
    private Set<ExerciseType> types = new HashSet<>();

    @Column(name = "base_reps", nullable = false)
    private Integer baseReps;

    @Column(name = "is_static_reps", nullable = false)
    private Boolean staticReps = false;

    @Column(name = "static_reps", nullable = false)
    private Integer staticRepsValue = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "reps_unit", nullable = false, length = 32)
    private RepsUnit repsUnit = RepsUnit.REPS;

    @Column(name = "comment")
    private String comment;

    @Column(name = "video_path", length = 1024)
    private String videoPath;

    @Column(name = "file_id", length = 512)
    private String fileId;

    @OneToMany(mappedBy = "exercise")
    private List<DayExercise> dayLinks = new ArrayList<>();
}
