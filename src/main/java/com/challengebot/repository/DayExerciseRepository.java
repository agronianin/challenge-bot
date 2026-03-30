package com.challengebot.repository;

import com.challengebot.model.DayExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayExerciseRepository extends JpaRepository<DayExercise, Integer> {
}
