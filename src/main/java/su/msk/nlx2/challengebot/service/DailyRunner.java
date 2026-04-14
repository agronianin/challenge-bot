package su.msk.nlx2.challengebot.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.msk.nlx2.challengebot.config.BotProperties;
import su.msk.nlx2.challengebot.model.DayExercise;
import su.msk.nlx2.challengebot.model.Exercise;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.model.ProgramDay;
import su.msk.nlx2.challengebot.model.ProgramDayMessage;
import su.msk.nlx2.challengebot.model.ProgramParticipant;
import su.msk.nlx2.challengebot.model.User;
import su.msk.nlx2.challengebot.model.bot.SentMessageInfo;
import su.msk.nlx2.challengebot.model.type.ProgramActionResult;
import su.msk.nlx2.challengebot.repository.DayExerciseRepository;
import su.msk.nlx2.challengebot.repository.ExerciseRepository;
import su.msk.nlx2.challengebot.repository.ProgramDayRepository;
import su.msk.nlx2.challengebot.repository.ProgramDayMessageRepository;
import su.msk.nlx2.challengebot.repository.ProgramParticipantRepository;
import su.msk.nlx2.challengebot.repository.ProgramRepository;
import su.msk.nlx2.challengebot.service.bot.MessageSender;
import su.msk.nlx2.challengebot.service.bot.keyboard.ChallengeKeyboardFactory;

@Service
@Slf4j
@RequiredArgsConstructor
public class DailyRunner {
    private static final List<String> PUBLISHABLE_PROGRAM_STATUSES = List.of("active", "scheduled");
    private static final String DAY_STATUS_PLANNED = "planned";
    private static final String DAY_STATUS_PUBLISHED = "published";

    private final ProgramRepository programRepository;
    private final ProgramDayRepository programDayRepository;
    private final DayExerciseRepository dayExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ProgramParticipantRepository programParticipantRepository;
    private final ProgramDayMessageRepository programDayMessageRepository;
    private final ExerciseSelector exerciseSelector;
    private final RepsCalculator repsCalculator;
    private final MessageSender messageSender;
    private final BotMessages botMessages;
    private final BotProperties botProperties;
    private final ChallengeKeyboardFactory challengeKeyboardFactory;

    @Scheduled(fixedDelayString = "${challenge.scheduler.publish-check-delay-ms:60000}")
    @Transactional
    public void publishDueDays() {
        for (Program program : programRepository.findByStatusIn(PUBLISHABLE_PROGRAM_STATUSES)) {
            publishDueDay(program);
        }
    }

    @Transactional
    public void runDaily(Integer programId) {
        programRepository.findById(programId).ifPresent(this::publishDueDay);
    }

    @Transactional
    public boolean sendCurrentPublishedDayToUser(Integer programId, User user, Locale locale) {
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            return false;
        }
        LocalDate today = LocalDate.now(resolveZoneId(program));
        ProgramDay day = programDayRepository.findByProgram_IdAndDateAndStatus(programId, today, DAY_STATUS_PUBLISHED).orElse(null);
        if (day == null) {
            return false;
        }
        List<DayExercise> dayExercises = dayExerciseRepository.findByProgramDay_IdOrderById(day.getId());
        sendPersonalDay(day, dayExercises, user, locale);
        return true;
    }

    @Transactional
    public ProgramActionResult republishLastPublishedDay(Integer programId) {
        if (!programRepository.existsById(programId)) {
            return ProgramActionResult.NOT_FOUND;
        }
        ProgramDay day = programDayRepository.findFirstByProgram_IdAndStatusOrderByDayIndexDesc(programId, DAY_STATUS_PUBLISHED).orElse(null);
        if (day == null) {
            return ProgramActionResult.NO_CHANGES;
        }
        List<DayExercise> dayExercises = getOrCreateDayExercises(day);
        deleteGroupMessages(day);
        sendGroupDay(day, dayExercises);
        return ProgramActionResult.SUCCESS;
    }

    private void publishDueDay(Program program) {
        ZoneId zoneId = resolveZoneId(program);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate today = now.toLocalDate();
        LocalTime postTime = parsePostTime(program);
        if (now.toLocalTime().isBefore(postTime)) {
            return;
        }
        ProgramDay day = programDayRepository.findByProgram_IdAndDate(program.getId(), today).orElse(null);
        if (day == null || !DAY_STATUS_PLANNED.equals(day.getStatus())) {
            return;
        }
        List<DayExercise> dayExercises = getOrCreateDayExercises(day);
        if (dayExercises.isEmpty()) {
            log.warn("No exercises selected for programId={}, dayId={}", program.getId(), day.getId());
            return;
        }
        sendGroupDay(day, dayExercises);
        day.setStatus(DAY_STATUS_PUBLISHED);
        programDayRepository.save(day);
        if ("scheduled".equals(program.getStatus())) {
            program.setStatus("active");
            programRepository.save(program);
        }
        sendPersonalDayToParticipants(day, dayExercises);
    }

    private List<DayExercise> getOrCreateDayExercises(ProgramDay day) {
        List<DayExercise> existingExercises = dayExerciseRepository.findByProgramDay_IdOrderById(day.getId());
        if (!existingExercises.isEmpty()) {
            return existingExercises;
        }
        List<Exercise> selectedExercises = exerciseSelector.selectDailyExercises(
                exerciseRepository.findAll(),
                day.getProgram().getTypesPerDay(),
                day.getProgram().getExercisesPerDay(),
                previousDayTypeNames(day),
                previousExerciseIds(day)
        );
        List<DayExercise> dayExercises = selectedExercises.stream()
                .map(exercise -> createDayExercise(day, exercise))
                .toList();
        return dayExerciseRepository.saveAll(dayExercises);
    }

    private DayExercise createDayExercise(ProgramDay day, Exercise exercise) {
        DayExercise dayExercise = new DayExercise();
        dayExercise.setProgramDay(day);
        dayExercise.setExercise(exercise);
        dayExercise.setReps(repsCalculator.calculateProgramReps(exercise, day.getDayIndex(), botProperties.getRepsGrowthPercent(), botProperties.getRepsRoundMode()));
        return dayExercise;
    }

    private void sendGroupDay(ProgramDay day, List<DayExercise> dayExercises) {
        Locale locale = botMessages.defaultLocale();
        long chatId = day.getProgram().getChat().getTgChatId();
        saveGroupMessage(day, messageSender.sendTextWithResult(chatId, botMessages.text(locale, "challenge.daily.group.header", day.getDayIndex())));
        for (DayExercise dayExercise : dayExercises) {
            Exercise exercise = dayExercise.getExercise();
            SentMessageInfo sentMessage = messageSender.sendVideo(chatId, exercise, groupExerciseCaption(exercise, locale));
            saveGroupMessage(day, sentMessage);
            if (sentMessage.fileId() != null && !sentMessage.fileId().equals(exercise.getFileId())) {
                exercise.setFileId(sentMessage.fileId());
                exerciseRepository.save(exercise);
            }
        }
        saveGroupMessage(day, messageSender.sendTextWithResult(
                chatId,
                botMessages.text(locale, "challenge.daily.group.join_hint"),
                challengeKeyboardFactory.joinChallengeKeyboard(locale, day.getProgram().getId())
        ));
    }

    private void sendPersonalDayToParticipants(ProgramDay day, List<DayExercise> dayExercises) {
        for (ProgramParticipant participant : programParticipantRepository.findByProgram_IdAndActiveTrue(day.getProgram().getId())) {
            Locale locale = botMessages.resolveLocale(participant.getUser(), null);
            sendPersonalDay(day, dayExercises, participant.getUser(), locale);
        }
    }

    private void sendPersonalDay(ProgramDay day, List<DayExercise> dayExercises, User user, Locale locale) {
        messageSender.sendText(user.getTgId(), botMessages.text(locale, "challenge.daily.private.header", day.getDayIndex()));
        for (DayExercise dayExercise : dayExercises) {
            messageSender.sendVideo(user.getTgId(), dayExercise.getExercise(), personalExerciseCaption(dayExercise, user, locale));
        }
    }

    private void saveGroupMessage(ProgramDay day, SentMessageInfo sentMessage) {
        if (sentMessage.messageId() == null) {
            return;
        }
        ProgramDayMessage dayMessage = new ProgramDayMessage();
        dayMessage.setProgramDay(day);
        dayMessage.setTgChatId(day.getProgram().getChat().getTgChatId());
        dayMessage.setMessageId(sentMessage.messageId());
        dayMessage.setCreatedAt(java.time.LocalDateTime.now());
        programDayMessageRepository.save(dayMessage);
    }

    private void deleteGroupMessages(ProgramDay day) {
        List<ProgramDayMessage> messages = programDayMessageRepository.findByProgramDay_IdOrderById(day.getId());
        for (ProgramDayMessage message : messages) {
            boolean deleted = messageSender.deleteMessage(message.getTgChatId(), message.getMessageId());
            if (!deleted) {
                log.warn("Could not delete Telegram message chatId={}, messageId={}", message.getTgChatId(), message.getMessageId());
            }
        }
        programDayMessageRepository.deleteByProgramDay_Id(day.getId());
    }

    private String groupExerciseCaption(Exercise exercise, Locale locale) {
        String comment = exercise.getComment() == null || exercise.getComment().isBlank()
                ? ""
                : "\n" + escapeHtml(exercise.getComment());
        return botMessages.text(locale, "challenge.daily.group.exercise_caption", escapeHtml(exercise.getName()), comment);
    }

    private String personalExerciseCaption(DayExercise dayExercise, User user, Locale locale) {
        int reps = repsCalculator.calculatePersonalReps(dayExercise, user, botProperties.getRepsGrowthPercent(), botProperties.getRepsRoundMode());
        String repsUnit = botMessages.text(locale, "challenge.daily.reps_unit." + dayExercise.getExercise().getRepsUnit().name());
        String comment = dayExercise.getExercise().getComment() == null || dayExercise.getExercise().getComment().isBlank()
                ? ""
                : "\n" + escapeHtml(dayExercise.getExercise().getComment());
        return botMessages.text(locale, "challenge.daily.private.exercise_caption", escapeHtml(dayExercise.getExercise().getName()), reps, repsUnit, comment);
    }

    private Set<String> previousDayTypeNames(ProgramDay day) {
        return programDayRepository.findByProgram_Id(day.getProgram().getId()).stream()
                .filter(programDay -> programDay.getDayIndex() == day.getDayIndex() - 1)
                .findFirst()
                .map(programDay -> dayExerciseRepository.findByProgramDay_IdOrderById(programDay.getId()).stream()
                        .flatMap(dayExercise -> dayExercise.getExercise().getTypes().stream())
                        .map(type -> type.getName())
                        .collect(java.util.stream.Collectors.toSet()))
                .orElse(Set.of());
    }

    private Set<Integer> previousExerciseIds(ProgramDay day) {
        Set<Integer> exerciseIds = new HashSet<>();
        for (ProgramDay programDay : programDayRepository.findByProgram_Id(day.getProgram().getId())) {
            if (programDay.getDayIndex() >= day.getDayIndex()) {
                continue;
            }
            dayExerciseRepository.findByProgramDay_IdOrderById(programDay.getId()).stream()
                    .map(dayExercise -> dayExercise.getExercise().getId())
                    .forEach(exerciseIds::add);
        }
        return exerciseIds;
    }

    private ZoneId resolveZoneId(Program program) {
        try {
            return ZoneId.of(program.getTimezone());
        } catch (Exception e) {
            log.warn("Invalid timezone for programId={}: {}", program.getId(), program.getTimezone());
            return ZoneId.of(botProperties.getTimezone());
        }
    }

    private LocalTime parsePostTime(Program program) {
        try {
            return LocalTime.parse(program.getPostTime());
        } catch (Exception e) {
            log.warn("Invalid postTime for programId={}: {}", program.getId(), program.getPostTime());
            return LocalTime.parse(botProperties.getPostTime());
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
