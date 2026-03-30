package com.challengebot.repository;

import com.challengebot.model.Chat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Optional<Chat> findByTgChatId(Long tgChatId);
}
