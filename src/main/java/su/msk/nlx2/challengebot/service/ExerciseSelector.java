package su.msk.nlx2.challengebot.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.Exercise;
import su.msk.nlx2.challengebot.model.ExerciseType;

@Service
public class ExerciseSelector {
    private static final String FIRST_DAILY_EXERCISE_GROUP = "1";

    public List<Exercise> selectDailyExercises(
            List<Exercise> allExercises,
            int typesPerDay,
            int exercisesPerDay,
            Set<String> excludedTypeNames,
            Set<Integer> excludedExerciseIds
    ) {
        List<Exercise> selected = new ArrayList<>();
        selectFirstDailyExercise(allExercises, excludedExerciseIds).ifPresent(selected::add);
        int remainingExercisesPerDay = exercisesPerDay - selected.size();
        if (remainingExercisesPerDay <= 0) {
            return selected;
        }
        Set<Integer> regularExcludedExerciseIds = new HashSet<>(excludedExerciseIds);
        selected.stream()
                .map(Exercise::getId)
                .forEach(regularExcludedExerciseIds::add);
        selected.addAll(selectRegularExercises(allExercises, typesPerDay, remainingExercisesPerDay, excludedTypeNames, regularExcludedExerciseIds));
        return selected;
    }

    private List<Exercise> selectRegularExercises(
            List<Exercise> allExercises,
            int typesPerDay,
            int exercisesPerDay,
            Set<String> excludedTypeNames,
            Set<Integer> excludedExerciseIds
    ) {
        List<Exercise> candidates = filterExercises(regularExercises(allExercises), excludedExerciseIds);
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

    private Optional<Exercise> selectFirstDailyExercise(List<Exercise> allExercises, Set<Integer> excludedExerciseIds) {
        List<Exercise> groupExercises = allExercises.stream()
                .filter(this::isEnabled)
                .filter(exercise -> FIRST_DAILY_EXERCISE_GROUP.equals(normalizeSelectionGroup(exercise)))
                .sorted(Comparator.comparing(Exercise::getId))
                .toList();
        if (groupExercises.isEmpty()) {
            return Optional.empty();
        }
        return groupExercises.stream()
                .filter(exercise -> !excludedExerciseIds.contains(exercise.getId()))
                .findFirst()
                .or(() -> Optional.of(groupExercises.getFirst()));
    }

    private List<Exercise> regularExercises(List<Exercise> allExercises) {
        return allExercises.stream()
                .filter(this::isEnabled)
                .filter(exercise -> normalizeSelectionGroup(exercise) == null)
                .toList();
    }

    private boolean isEnabled(Exercise exercise) {
        return Boolean.TRUE.equals(exercise.getEnabled());
    }

    private String normalizeSelectionGroup(Exercise exercise) {
        if (exercise.getSelectionGroup() == null || exercise.getSelectionGroup().isBlank()) {
            return null;
        }
        return exercise.getSelectionGroup().trim();
    }

    private List<Exercise> filterExercises(List<Exercise> allExercises, Set<Integer> excludedExerciseIds) {
        List<Exercise> candidates = allExercises.stream()
                .filter(exercise -> !excludedExerciseIds.contains(exercise.getId()))
                .toList();
        return candidates.isEmpty() ? allExercises : candidates;
    }

    private Set<String> exerciseTypeNames(Exercise exercise) {
        return exercise.getTypes().stream()
                .map(ExerciseType::getName)
                .collect(java.util.stream.Collectors.toSet());
    }
}
