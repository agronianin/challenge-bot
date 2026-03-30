package com.challengebot.repository;

import com.challengebot.model.Program;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    List<Program> findByStatus(String status);
}
