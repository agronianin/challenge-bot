package su.msk.nlx2.challengebot.model.bot;

import su.msk.nlx2.challengebot.model.type.ChallengeJoinStatus;

public record ChallengeJoinResult(
        ChallengeJoinStatus status,
        Integer programId
) {
}
