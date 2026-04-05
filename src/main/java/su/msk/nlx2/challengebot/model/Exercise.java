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
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "exercises",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_exercises_name_group", columnNames = {"name", "group_id"})
        }
)
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ExerciseGroup group;

    @Column(name = "base_reps", nullable = false)
    private Integer baseReps;

    @Column(name = "comment")
    private String comment;

    @Column(name = "video_path", length = 1024)
    private String videoPath;

    @Column(name = "file_id", length = 512)
    private String fileId;

    @OneToMany(mappedBy = "exercise")
    private List<DayExercise> dayLinks = new ArrayList<>();
}
