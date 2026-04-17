package su.msk.nlx2.challengebot.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.model.type.ProgramStatus;

public interface ProgramRepository extends JpaRepository<Program, Integer> {

    List<Program> findByStatusIn(Collection<ProgramStatus> statuses);

    List<Program> findByStatusInOrderByStartDateDescIdDesc(Collection<ProgramStatus> statuses);

    boolean existsByChat_TgChatIdAndStatusIn(Long tgChatId, Collection<ProgramStatus> statuses);
}
