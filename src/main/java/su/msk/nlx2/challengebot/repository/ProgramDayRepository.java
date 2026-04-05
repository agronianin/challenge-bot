package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.ProgramDay;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramDayRepository extends JpaRepository<ProgramDay, Integer> {
    Optional<ProgramDay> findFirstByProgram_IdOrderByDayIndexDesc(Integer programId);
}
