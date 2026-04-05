package su.msk.nlx2.challengebot.bot;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class AdminConversationService {
    private final Map<Long, AdminSession> sessions = new ConcurrentHashMap<>();

    public AdminSession start(long tgUserId, AdminFlowStep step) {
        AdminSession session = new AdminSession();
        session.setStep(step);
        sessions.put(tgUserId, session);
        return session;
    }

    public Optional<AdminSession> find(long tgUserId) {
        return Optional.ofNullable(sessions.get(tgUserId));
    }

    public void clear(long tgUserId) {
        sessions.remove(tgUserId);
    }
}
