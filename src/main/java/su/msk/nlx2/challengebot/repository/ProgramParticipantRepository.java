package su.msk.nlx2.challengebot.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import su.msk.nlx2.challengebot.model.ProgramParticipant;

public interface ProgramParticipantRepository extends JpaRepository<ProgramParticipant, Integer> {
    List<ProgramParticipant> findByProgram_IdAndActiveTrue(Integer programId);
    Optional<ProgramParticipant> findByProgram_IdAndUser_Id(Integer programId, Integer userId);
    void deleteByProgram_Id(Integer programId);
}
