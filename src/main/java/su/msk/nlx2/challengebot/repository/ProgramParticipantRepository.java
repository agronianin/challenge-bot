package su.msk.nlx2.challengebot.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import su.msk.nlx2.challengebot.model.ProgramParticipant;
import su.msk.nlx2.challengebot.model.type.ProgramStatus;

public interface ProgramParticipantRepository extends JpaRepository<ProgramParticipant, Integer> {
    List<ProgramParticipant> findByProgram_IdAndActiveTrue(Integer programId);
    List<ProgramParticipant> findByUser_TgIdAndActiveTrueAndProgram_StatusIn(Long tgUserId, Collection<ProgramStatus> statuses);
    Optional<ProgramParticipant> findByProgram_IdAndUser_Id(Integer programId, Integer userId);
    boolean existsByUser_TgIdAndActiveTrueAndProgram_StatusIn(Long tgUserId, Collection<ProgramStatus> statuses);
    void deleteByProgram_Id(Integer programId);
}
