package com.challengebot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class RepsCalculator {
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
}
