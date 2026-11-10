package su.msk.nlx2.challengebot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import su.msk.nlx2.challengebot.model.Exercise;

class ExerciseSelectorTest {
    private final ExerciseSelector exerciseSelector = new ExerciseSelector();

    @Test
    void selectsGroupOneExerciseAsFirstAndRepeatsWhenItIsTheOnlyGroupExercise() {
        Exercise repeatedExercise = exercise(1, "Подтягивания", "1");
        Exercise regularExercise = exercise(2, "Отжимания", null);

        List<Exercise> selected = exerciseSelector.selectDailyExercises(
                List.of(repeatedExercise, regularExercise),
                2,
                2,
                Set.of(),
                Set.of(1)
        );

        assertEquals(List.of(1, 2), selected.stream().map(Exercise::getId).toList());
    }

    @Test
    void selectsRemainingExercisesOnlyFromExercisesWithoutSelectionGroup() {
        Exercise firstGroupExercise = exercise(1, "Подтягивания", "1");
        Exercise otherGroupExercise = exercise(2, "Отдельная группа", "2");
        Exercise firstRegularExercise = exercise(3, "Отжимания", null);
        Exercise secondRegularExercise = exercise(4, "Планка", "");

        List<Exercise> selected = exerciseSelector.selectDailyExercises(
                List.of(firstGroupExercise, otherGroupExercise, firstRegularExercise, secondRegularExercise),
                2,
                3,
                Set.of(),
                Set.of()
        );

        assertEquals(List.of(1, 3, 4), selected.stream().map(Exercise::getId).toList());
    }

    @Test
    void ignoresDisabledExercisesForFirstAndRegularSlots() {
        Exercise disabledGroupExercise = exercise(1, "Подтягивания", "1", false);
        Exercise enabledGroupExercise = exercise(2, "Австралийские подтягивания", "1", true);
        Exercise disabledRegularExercise = exercise(3, "Отжимания", null, false);
        Exercise enabledRegularExercise = exercise(4, "Планка", null, true);

        List<Exercise> selected = exerciseSelector.selectDailyExercises(
                List.of(disabledGroupExercise, enabledGroupExercise, disabledRegularExercise, enabledRegularExercise),
                2,
                2,
                Set.of(),
                Set.of()
        );

        assertEquals(List.of(2, 4), selected.stream().map(Exercise::getId).toList());
    }

    private Exercise exercise(Integer id, String name, String selectionGroup) {
        return exercise(id, name, selectionGroup, true);
    }

    private Exercise exercise(Integer id, String name, String selectionGroup, boolean enabled) {
        Exercise exercise = new Exercise();
        exercise.setId(id);
        exercise.setName(name);
        exercise.setEnabled(enabled);
        exercise.setSelectionGroup(selectionGroup);
        return exercise;
    }
}
