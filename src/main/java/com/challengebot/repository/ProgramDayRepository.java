package com.challengebot.repository;

import com.challengebot.model.ProgramDay;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramDayRepository extends JpaRepository<ProgramDay, Integer> {
    Optional<ProgramDay> findFirstByProgram_IdOrderByDayIndexDesc(Integer programId);
}
