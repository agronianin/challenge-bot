package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.DayExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayExerciseRepository extends JpaRepository<DayExercise, Integer> {
}
