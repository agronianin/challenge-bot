package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.ExerciseGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseGroupRepository extends JpaRepository<ExerciseGroup, Integer> {
    Optional<ExerciseGroup> findByName(String name);
}
