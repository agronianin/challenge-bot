package su.msk.nlx2.challengebot.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.model.bot.AdminChallengeView;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.Chat;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.model.ProgramDay;
import su.msk.nlx2.challengebot.model.type.ProgramActionResult;
import su.msk.nlx2.challengebot.repository.ChatRepository;
import su.msk.nlx2.challengebot.repository.CompletionRepository;
import su.msk.nlx2.challengebot.repository.DayExerciseRepository;
import su.msk.nlx2.challengebot.repository.ProgramParticipantRepository;
import su.msk.nlx2.challengebot.repository.ProgramDayRepository;
import su.msk.nlx2.challengebot.repository.ProgramDayMessageRepository;
import su.msk.nlx2.challengebot.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeAdminService {
    private final ChatRepository chatRepository;
    private final ProgramRepository programRepository;
    private final ProgramDayRepository programDayRepository;
    private final DayExerciseRepository dayExerciseRepository;
    private final CompletionRepository completionRepository;
    private final ProgramParticipantRepository programParticipantRepository;
    private final ProgramDayMessageRepository programDayMessageRepository;

    public boolean hasActiveOrScheduledProgram(Long tgChatId) {
        return programRepository.existsByChat_TgChatIdAndStatusIn(tgChatId, List.of("active", "scheduled"));
    }

    @Transactional(readOnly = true)
    public List<AdminChallengeView> findManageablePrograms() {
        return programRepository.findByStatusInOrderByStartDateDescIdDesc(List.of("active", "scheduled", "paused")).stream()
                .map(program -> new AdminChallengeView(
                        program.getId(),
                        program.getChat().getTitle() != null ? program.getChat().getTitle() : "chat " + program.getChat().getTgChatId(),
                        program.getStartDate(),
                        program.getPostTime(),
                        program.getTimezone(),
                        program.getDaysTotal(),
                        program.getStatus()
                ))
                .toList();
    }

    @Transactional
    public Program createProgram(ConversationSession session) {
        Chat chat = chatRepository.findByTgChatId(session.getChatTgId())
                .map(existing -> updateChat(existing, session))
                .orElseGet(() -> createChat(session));

        Program program = new Program();
        program.setChat(chat);
        program.setDaysTotal(session.getDaysTotal());
        program.setStartDate(session.getStartDate());
        program.setPostTime(session.getPostTime());
        program.setTimezone(session.getTimezone());
        program.setExercisesPerDay(session.getExercisesPerDay());
        program.setTypesPerDay(session.getTypesPerDay());
        program.setRestDayFrequency(0);
        program.setStatus(session.getStartDate().isAfter(LocalDate.now()) ? "scheduled" : "active");
        Program savedProgram = programRepository.save(program);

        List<ProgramDay> days = new ArrayList<>();
        for (int i = 0; i < session.getDaysTotal(); i++) {
            ProgramDay day = new ProgramDay();
            day.setProgram(savedProgram);
            day.setDayIndex(i + 1);
            day.setDate(session.getStartDate().plusDays(i));
            day.setStatus("planned");
            days.add(day);
        }
        programDayRepository.saveAll(days);

        return savedProgram;
    }

    @Transactional
    public ProgramActionResult pauseProgram(Integer programId) {
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            return ProgramActionResult.NOT_FOUND;
        }
        if ("paused".equals(program.getStatus())) {
            return ProgramActionResult.NO_CHANGES;
        }
        program.setStatus("paused");
        programRepository.save(program);
        return ProgramActionResult.SUCCESS;
    }

    @Transactional
    public ProgramActionResult deleteProgram(Integer programId) {
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            return ProgramActionResult.NOT_FOUND;
        }
        completionRepository.deleteByProgramDay_Program_Id(programId);
        programDayMessageRepository.deleteByProgramDay_Program_Id(programId);
        dayExerciseRepository.deleteByProgramDay_Program_Id(programId);
        programDayRepository.deleteByProgram_Id(programId);
        programParticipantRepository.deleteByProgram_Id(programId);
        programRepository.delete(program);
        return ProgramActionResult.SUCCESS;
    }

    private Chat createChat(ConversationSession session) {
        Chat chat = new Chat();
        chat.setTgChatId(session.getChatTgId());
        chat.setTitle(session.getChatTitle());
        return chatRepository.save(chat);
    }

    private Chat updateChat(Chat existing, ConversationSession session) {
        existing.setTitle(session.getChatTitle());
        return chatRepository.save(existing);
    }
}
