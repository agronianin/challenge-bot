package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.ExerciseType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, String> {
    Optional<ExerciseType> findByName(String name);
}
