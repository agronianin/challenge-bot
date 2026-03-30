package com.challengebot.repository;

import com.challengebot.model.Completion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletionRepository extends JpaRepository<Completion, Integer> {
}
