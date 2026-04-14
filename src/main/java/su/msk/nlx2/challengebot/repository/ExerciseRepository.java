package su.msk.nlx2.challengebot.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import su.msk.nlx2.challengebot.model.Exercise;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    @Query("select distinct e from Exercise e join e.types g where g.name in :typeNames")
    List<Exercise> findByTypeNames(@Param("typeNames") List<String> typeNames);

    Optional<Exercise> findByName(String name);
}
