package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.Completion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionRepository extends JpaRepository<Completion, Integer> {
    void deleteByProgramDay_Program_Id(Integer programId);
}
