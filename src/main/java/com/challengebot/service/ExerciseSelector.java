package com.challengebot.service;

import com.challengebot.model.Exercise;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ExerciseSelector {
    public List<Exercise> selectDailyExercises(
            List<Exercise> allExercises,
            int groupsPerDay,
            int exercisesPerDay,
            Set<Integer> excludedGroupIds,
            Set<Integer> excludedExerciseIds
    ) {
        // TODO: implement selection algorithm
        throw new UnsupportedOperationException("Not implemented");
    }
}
