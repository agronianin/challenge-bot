package su.msk.nlx2.challengebot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.DayExercise;
import su.msk.nlx2.challengebot.model.Exercise;
import su.msk.nlx2.challengebot.model.User;

@Service
public class RepsCalculator {
    private static final int BASELINE_PULL_UPS = 10;
    private static final double MIN_USER_MULTIPLIER = 0.2;

    public int calculate(int baseReps, int dayIndex, double growthPercent, String roundMode) {
        double factor = 1 + growthPercent * (dayIndex - 1);
        double value = baseReps * factor;

        return switch (roundMode) {
            case "ceil" -> (int) Math.ceil(value);
            case "floor" -> (int) Math.floor(value);
            case "round" -> BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).intValue();
            default -> throw new IllegalArgumentException("Unsupported roundMode: " + roundMode);
        };
    }

    public int calculateProgramReps(Exercise exercise, int dayIndex, double growthPercent, String roundMode) {
        return calculatePersonalReps(exercise, dayIndex, growthPercent, roundMode, BASELINE_PULL_UPS);
    }

    public int calculatePersonalReps(DayExercise dayExercise, User user, double growthPercent, String roundMode) {
        return calculatePersonalReps(dayExercise.getExercise(), dayExercise.getProgramDay().getDayIndex(), growthPercent, roundMode, user.getMaxPullUps());
    }

    private int calculatePersonalReps(Exercise exercise, int dayIndex, double growthPercent, String roundMode, Integer maxPullUps) {
        if (Boolean.TRUE.equals(exercise.getStaticReps())) {
            return exercise.getStaticRepsValue();
        }
        int calculatedBase = calculate(exercise.getBaseReps(), dayIndex, growthPercent, roundMode);
        int personalBase = (int) Math.ceil(calculatedBase * userMultiplier(maxPullUps));
        return exercise.getStaticRepsValue() + personalBase;
    }

    private double userMultiplier(Integer maxPullUps) {
        if (maxPullUps == null) {
            return 1;
        }
        return Math.max(MIN_USER_MULTIPLIER, (double) maxPullUps / BASELINE_PULL_UPS);
    }
}
