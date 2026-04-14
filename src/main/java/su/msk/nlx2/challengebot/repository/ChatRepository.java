package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.Chat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByTgChatId(Long tgChatId);
}
