package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.Program;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    List<Program> findByStatus(String status);
    List<Program> findByStatusIn(Collection<String> statuses);
    List<Program> findByStatusInOrderByStartDateDescIdDesc(Collection<String> statuses);
    boolean existsByChat_TgChatIdAndStatusIn(Long tgChatId, Collection<String> statuses);
}
