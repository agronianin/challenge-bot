package su.msk.nlx2.challengebot.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.Chat;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.model.ProgramDay;
import su.msk.nlx2.challengebot.repository.ChatRepository;
import su.msk.nlx2.challengebot.repository.ProgramDayRepository;
import su.msk.nlx2.challengebot.repository.ProgramRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChallengeAdminService {
    private final ChatRepository chatRepository;
    private final ProgramRepository programRepository;
    private final ProgramDayRepository programDayRepository;

    public boolean hasActiveOrScheduledProgram(Long tgChatId) {
        return chatRepository.findByTgChatId(tgChatId)
                .map(chat -> programRepository.existsByChat_IdAndStatusIn(chat.getId(), List.of("active", "scheduled")))
                .orElse(false);
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
        program.setGroupsPerDay(session.getGroupsPerDay());
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
