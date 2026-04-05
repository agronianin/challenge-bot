package su.msk.nlx2.challengebot.repository;

import su.msk.nlx2.challengebot.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByTgId(Long tgId);
}
