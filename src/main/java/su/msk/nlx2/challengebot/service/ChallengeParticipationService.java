package su.msk.nlx2.challengebot.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import su.msk.nlx2.challengebot.model.Program;
import su.msk.nlx2.challengebot.model.ProgramParticipant;
import su.msk.nlx2.challengebot.model.TgUser;
import su.msk.nlx2.challengebot.model.bot.ChallengeJoinResult;
import su.msk.nlx2.challengebot.model.type.ChallengeJoinStatus;
import su.msk.nlx2.challengebot.model.type.ProgramStatus;
import su.msk.nlx2.challengebot.repository.ProgramParticipantRepository;
import su.msk.nlx2.challengebot.repository.ProgramRepository;
import su.msk.nlx2.challengebot.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class ChallengeParticipationService {
    private static final List<ProgramStatus> JOINABLE_STATUSES = List.of(ProgramStatus.ACTIVE, ProgramStatus.SCHEDULED);
    private static final List<ProgramStatus> PARTICIPATION_STATUSES = List.of(ProgramStatus.ACTIVE, ProgramStatus.SCHEDULED, ProgramStatus.PAUSED);

    private final ProgramRepository programRepository;
    private final ProgramParticipantRepository programParticipantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChallengeJoinResult join(TgUser user, Integer programId) {
        TgUser managedUser = userRepository.findById(user.getId()).orElse(null);
        if (managedUser == null) {
            return new ChallengeJoinResult(ChallengeJoinStatus.SETUP_REQUIRED, programId);
        }
        if (user.getMaxPullUps() == null) {
            return new ChallengeJoinResult(ChallengeJoinStatus.SETUP_REQUIRED, programId);
        }
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            return new ChallengeJoinResult(ChallengeJoinStatus.PROGRAM_NOT_FOUND, programId);
        }
        if (!JOINABLE_STATUSES.contains(program.getStatus())) {
            return new ChallengeJoinResult(ChallengeJoinStatus.PROGRAM_NOT_ACTIVE, programId);
        }
        ProgramParticipant participant = programParticipantRepository.findByProgram_IdAndUser_Id(programId, user.getId()).orElse(null);
        if (participant != null) {
            if (Boolean.TRUE.equals(participant.getActive())) {
                return new ChallengeJoinResult(ChallengeJoinStatus.ALREADY_ACTIVE_PARTICIPATION, programId);
            }
            if (hasActiveParticipation(user.getTgId())) {
                return new ChallengeJoinResult(ChallengeJoinStatus.ALREADY_ACTIVE_PARTICIPATION, programId);
            }
            if (Boolean.FALSE.equals(participant.getActive())) {
                participant.setActive(true);
                programParticipantRepository.save(participant);
                return new ChallengeJoinResult(ChallengeJoinStatus.SUCCESS, programId);
            }
        }
        if (hasActiveParticipation(user.getTgId())) {
            return new ChallengeJoinResult(ChallengeJoinStatus.ALREADY_ACTIVE_PARTICIPATION, programId);
        }
        participant = new ProgramParticipant();
        participant.setProgram(program);
        participant.setUser(managedUser);
        participant.setJoinedAt(LocalDateTime.now());
        participant.setActive(true);
        programParticipantRepository.save(participant);
        return new ChallengeJoinResult(ChallengeJoinStatus.SUCCESS, programId);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveParticipation(Long tgUserId) {
        return programParticipantRepository.existsByUser_TgIdAndActiveTrueAndProgram_StatusIn(tgUserId, PARTICIPATION_STATUSES);
    }

    @Transactional
    public int giveUpActiveParticipations(Long tgUserId) {
        List<ProgramParticipant> participants = programParticipantRepository.findByUser_TgIdAndActiveTrueAndProgram_StatusIn(
                tgUserId,
                PARTICIPATION_STATUSES
        );
        for (ProgramParticipant participant : participants) {
            participant.setActive(false);
        }
        programParticipantRepository.saveAll(participants);
        return participants.size();
    }
}
