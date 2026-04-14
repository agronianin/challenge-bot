package su.msk.nlx2.challengebot.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.Exercise;

@Service
public class ExerciseSelector {
    public List<Exercise> selectDailyExercises(
            List<Exercise> allExercises,
            int typesPerDay,
            int exercisesPerDay,
            Set<String> excludedTypeNames,
            Set<Integer> excludedExerciseIds
    ) {
        List<Exercise> candidates = filterExercises(allExercises, excludedExerciseIds);
        List<Exercise> withoutExcludedTypes = candidates.stream()
                .filter(exercise -> exerciseTypeNames(exercise).stream().noneMatch(excludedTypeNames::contains))
                .toList();
        if (withoutExcludedTypes.size() >= exercisesPerDay) {
            candidates = withoutExcludedTypes;
        }
        List<Exercise> selected = new ArrayList<>();
        Set<String> selectedTypeNames = new HashSet<>();
        for (Exercise exercise : candidates.stream().sorted(Comparator.comparing(Exercise::getId)).toList()) {
            if (selected.size() >= exercisesPerDay) {
                break;
            }
            Set<String> exerciseTypeNames = exerciseTypeNames(exercise);
            Set<String> newTypeNames = new HashSet<>(exerciseTypeNames);
            newTypeNames.removeAll(selectedTypeNames);
            if (selectedTypeNames.size() + newTypeNames.size() > typesPerDay) {
                continue;
            }
            selected.add(exercise);
            selectedTypeNames.addAll(exerciseTypeNames);
        }
        if (selected.size() >= exercisesPerDay) {
            return selected;
        }
        for (Exercise exercise : candidates.stream().sorted(Comparator.comparing(Exercise::getId)).toList()) {
            if (selected.size() >= exercisesPerDay) {
                break;
            }
            if (!selected.contains(exercise)) {
                selected.add(exercise);
            }
        }
        return selected;
    }

    private List<Exercise> filterExercises(List<Exercise> allExercises, Set<Integer> excludedExerciseIds) {
        List<Exercise> candidates = allExercises.stream()
                .filter(exercise -> !excludedExerciseIds.contains(exercise.getId()))
                .toList();
        return candidates.isEmpty() ? allExercises : candidates;
    }

    private Set<String> exerciseTypeNames(Exercise exercise) {
        return exercise.getTypes().stream()
                .map(type -> type.getName())
                .collect(java.util.stream.Collectors.toSet());
    }
}
