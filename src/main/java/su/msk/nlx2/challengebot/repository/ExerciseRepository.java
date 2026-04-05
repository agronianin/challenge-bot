package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseRepository extends JpaRepository<Exercise, Integer> {
    @Query("select e from Exercise e where e.group.id in :groupIds")
    List<Exercise> findByGroupIds(@Param("groupIds") List<Integer> groupIds);
}
