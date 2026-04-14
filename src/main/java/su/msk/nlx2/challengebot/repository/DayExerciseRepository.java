package su.msk.nlx2.challengebot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import su.msk.nlx2.challengebot.model.DayExercise;

public interface DayExerciseRepository extends JpaRepository<DayExercise, Integer> {
    List<DayExercise> findByProgramDay_IdOrderById(Integer programDayId);
    void deleteByProgramDay_Program_Id(Integer programId);
}
