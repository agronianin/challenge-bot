package su.msk.nlx2.challengebot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import su.msk.nlx2.challengebot.model.ProgramDayMessage;

public interface ProgramDayMessageRepository extends JpaRepository<ProgramDayMessage, Integer> {
    List<ProgramDayMessage> findByProgramDay_IdOrderById(Integer programDayId);
    void deleteByProgramDay_Id(Integer programDayId);
    void deleteByProgramDay_Program_Id(Integer programId);
}
