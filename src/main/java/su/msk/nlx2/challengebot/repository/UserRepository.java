package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.TgUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<TgUser, Integer> {
    Optional<TgUser> findByTgId(Long tgId);
}
