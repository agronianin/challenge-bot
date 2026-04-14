package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "exercise_type")
public class ExerciseType {
    @Id
    @Column(name = "name", nullable = false, unique = true, length = 128)
    private String name;

    @ManyToMany(mappedBy = "types")
    private Set<Exercise> exercises = new HashSet<>();
}
