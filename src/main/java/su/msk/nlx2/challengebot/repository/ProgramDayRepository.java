package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.ProgramDay;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramDayRepository extends JpaRepository<ProgramDay, Integer> {
    List<ProgramDay> findByProgram_Id(Integer programId);
    Optional<ProgramDay> findByProgram_IdAndDate(Integer programId, LocalDate date);
    Optional<ProgramDay> findByProgram_IdAndDateAndStatus(Integer programId, LocalDate date, String status);
    Optional<ProgramDay> findFirstByProgram_IdAndStatusOrderByDayIndexDesc(Integer programId, String status);
    Optional<ProgramDay> findFirstByProgram_IdOrderByDayIndexDesc(Integer programId);
    void deleteByProgram_Id(Integer programId);
}
