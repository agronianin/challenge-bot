package su.msk.nlx2.challengebot.service.bot.conversation;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.message.ConversationSession;
import su.msk.nlx2.challengebot.model.type.ConversationStep;

@Service
public class ConversationService {
    private final Map<Long, ConversationSession> sessions = new ConcurrentHashMap<>();

    public ConversationSession start(long tgUserId, ConversationStep step) {
        ConversationSession session = new ConversationSession();
        session.setStep(step);
        sessions.put(tgUserId, session);
        return session;
    }

    public Optional<ConversationSession> find(long tgUserId) {
        return Optional.ofNullable(sessions.get(tgUserId));
    }

    public void clear(long tgUserId) {
        sessions.remove(tgUserId);
    }
}
