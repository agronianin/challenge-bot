package su.msk.nlx2.challengebot.service;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.msk.nlx2.challengebot.model.Exercise;
import su.msk.nlx2.challengebot.model.ExerciseType;
import su.msk.nlx2.challengebot.model.csv.ExerciseCsvImportResult;
import su.msk.nlx2.challengebot.model.type.RepsUnit;
import su.msk.nlx2.challengebot.repository.ExerciseTypeRepository;
import su.msk.nlx2.challengebot.repository.ExerciseRepository;

@Service
@RequiredArgsConstructor
public class CsvImporter {
    private static final String HEADER_NAME = "name";
    private static final String HEADER_TYPE_NAME = "type_name";
    private static final String HEADER_BASE_REPS = "base_reps";
    private static final String HEADER_IS_STATIC_REPS = "is_static_reps";
    private static final String HEADER_STATIC_REPS = "static_reps";
    private static final String HEADER_REPS_UNIT = "reps_unit";
    private static final String HEADER_VIDEO_PATH = "video_path";
    private static final String HEADER_COMMENT = "comment";

    private final ExerciseRepository exerciseRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    @Transactional
    public ExerciseCsvImportResult importExercises(Path csvPath) {
        int createdCount = 0;
        int updatedCount = 0;
        try (
                Reader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)
        ) {
            for (CSVRecord record : parser) {
                boolean created = upsertExercise(record);
                if (created) {
                    createdCount++;
                    continue;
                }
                updatedCount++;
            }
            return new ExerciseCsvImportResult(createdCount, updatedCount);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать CSV-файл.", e);
        }
    }

    private boolean upsertExercise(CSVRecord record) {
        String name = getRequiredValue(record, HEADER_NAME);
        Set<ExerciseType> types = getTypes(record);
        Integer baseReps = getNonNegativeInteger(record, HEADER_BASE_REPS);
        Boolean staticReps = getBoolean(record, HEADER_IS_STATIC_REPS);
        Integer staticRepsValue = getNonNegativeInteger(record, HEADER_STATIC_REPS);
        RepsUnit repsUnit = getRepsUnit(record);
        String videoPath = getOptionalValue(record, HEADER_VIDEO_PATH);
        String comment = getOptionalValue(record, HEADER_COMMENT);
        Exercise exercise = exerciseRepository.findByName(name).orElseGet(Exercise::new);
        boolean created = exercise.getId() == null;
        exercise.setName(name);
        exercise.getTypes().clear();
        exercise.getTypes().addAll(types);
        exercise.setBaseReps(baseReps);
        exercise.setStaticReps(staticReps);
        exercise.setStaticRepsValue(staticRepsValue);
        exercise.setRepsUnit(repsUnit);
        exercise.setVideoPath(videoPath);
        exercise.setComment(comment);
        exerciseRepository.save(exercise);
        return created;
    }

    private Set<ExerciseType> getTypes(CSVRecord record) {
        String value = getRequiredValue(record, HEADER_TYPE_NAME);
        List<String> typeNames = Arrays.stream(value.split("[;|]"))
                .map(String::trim)
                .filter(typeName -> !typeName.isEmpty())
                .distinct()
                .toList();
        if (typeNames.isEmpty()) {
            throw rowError(record, "Поле \"" + HEADER_TYPE_NAME + "\" должно содержать хотя бы один тип.");
        }
        return typeNames.stream()
                .map(typeName -> exerciseTypeRepository.findByName(typeName).orElseGet(() -> createType(typeName)))
                .collect(Collectors.toSet());
    }

    private ExerciseType createType(String typeName) {
        ExerciseType type = new ExerciseType();
        type.setName(typeName);
        return exerciseTypeRepository.save(type);
    }

    private String getRequiredValue(CSVRecord record, String header) {
        String value = getRawValue(record, header);
        if (value == null || value.isBlank()) {
            throw rowError(record, "Поле \"" + header + "\" обязательно.");
        }
        return value.trim();
    }

    private String getOptionalValue(CSVRecord record, String header) {
        if (!record.isMapped(header)) {
            return null;
        }
        String value = getRawValue(record, header);
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }

    private Integer getNonNegativeInteger(CSVRecord record, String header) {
        String value = getRequiredValue(record, header);
        try {
            int number = Integer.parseInt(value);
            if (number < 0) {
                throw rowError(record, "Поле \"" + header + "\" должно быть целым числом 0 или больше.");
            }
            return number;
        } catch (NumberFormatException e) {
            throw rowError(record, "Поле \"" + header + "\" должно быть целым числом 0 или больше.");
        }
    }

    private Boolean getBoolean(CSVRecord record, String header) {
        String value = getRequiredValue(record, header).toLowerCase();
        return switch (value) {
            case "true", "1", "yes", "y", "да" -> true;
            case "false", "0", "no", "n", "нет" -> false;
            default -> throw rowError(record, "Поле \"" + header + "\" должно содержать true/false.");
        };
    }

    private RepsUnit getRepsUnit(CSVRecord record) {
        String value = getOptionalValue(record, HEADER_REPS_UNIT);
        if (value == null) {
            return RepsUnit.REPS;
        }
        return switch (value.trim().toLowerCase()) {
            case "repetitions", "repetition", "reps", "rep", "повторения", "повторы", "раз" -> RepsUnit.REPS;
            case "seconds", "second", "sec", "secs", "s", "секунды", "сек" -> RepsUnit.SEC;
            default -> throw rowError(record, "Поле \"" + HEADER_REPS_UNIT + "\" должно содержать repetitions или seconds.");
        };
    }

    private String getRawValue(CSVRecord record, String header) {
        try {
            return record.get(header);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("В CSV отсутствует обязательная колонка \"" + header + "\".");
        }
    }

    private IllegalArgumentException rowError(CSVRecord record, String message) {
        return new IllegalArgumentException("Строка " + (record.getRecordNumber() + 1) + ": " + message);
    }
}
