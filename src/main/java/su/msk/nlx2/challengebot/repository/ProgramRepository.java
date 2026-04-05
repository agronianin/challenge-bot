package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.Program;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    List<Program> findByStatus(String status);
    boolean existsByChat_IdAndStatusIn(Integer chatId, Collection<String> statuses);
}
