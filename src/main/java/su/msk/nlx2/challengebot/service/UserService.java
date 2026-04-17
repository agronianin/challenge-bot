package su.msk.nlx2.challengebot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.shared.SharedUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.TgUser;
import su.msk.nlx2.challengebot.model.type.UserRole;
import su.msk.nlx2.challengebot.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<TgUser> findByTgId(Long tgId) {
        return userRepository.findByTgId(tgId);
    }

    public TgUser getRequiredByTgId(Long tgId) {
        return userRepository.findByTgId(tgId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for tgId=" + tgId));
    }

    public boolean isAdmin(Long tgId) {
        return userRepository.findByTgId(tgId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    public TgUser syncFromMessage(Message message) {
        User from = message.from();
        if (from == null) {
            throw new IllegalArgumentException("Message sender is missing");
        }
        return syncFromTelegram(from);
    }

    public TgUser syncFromTelegram(User from) {
        return userRepository.findByTgId(from.id())
                .map(existing -> updateFromTelegram(existing, from))
                .orElseGet(() -> createFromTelegram(from));
    }

    public TgUser promoteToAdmin(SharedUser sharedUser) {
        return userRepository.findByTgId(sharedUser.userId())
                .map(existing -> updatePromotedUser(existing, sharedUser))
                .orElseGet(() -> createPromotedUser(sharedUser));
    }

    public TgUser setMaxPullUps(Long tgId, int maxPullUps) {
        TgUser user = getRequiredByTgId(tgId);
        user.setMaxPullUps(maxPullUps);
        return userRepository.save(user);
    }

    public TgUser setLocale(Long tgId, String localeCode) {
        TgUser user = getRequiredByTgId(tgId);
        user.setLocaleCode(localeCode);
        return userRepository.save(user);
    }

    private TgUser createFromTelegram(User from) {
        TgUser user = new TgUser();
        user.setTgId(from.id());
        user.setName(buildDisplayName(from.firstName(), from.lastName(), from.username()));
        return userRepository.save(user);
    }

    private TgUser updateFromTelegram(TgUser existing, User from) {
        existing.setName(buildDisplayName(from.firstName(), from.lastName(), from.username()));
        return userRepository.save(existing);
    }

    private TgUser createPromotedUser(SharedUser sharedUser) {
        TgUser user = new TgUser();
        user.setTgId(sharedUser.userId());
        user.setName(buildDisplayName(sharedUser.firstName(), sharedUser.lastName(), sharedUser.username()));
        user.setRole(UserRole.ADMIN);
        return userRepository.save(user);
    }

    private TgUser updatePromotedUser(TgUser existing, SharedUser sharedUser) {
        existing.setName(buildDisplayName(sharedUser.firstName(), sharedUser.lastName(), sharedUser.username()));
        existing.setRole(UserRole.ADMIN);
        return userRepository.save(existing);
    }

    private String buildDisplayName(String firstName, String lastName, String username) {
        String name = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        if (!name.isBlank()) {
            return name;
        }
        if (username != null && !username.isBlank()) {
            return "@" + username;
        }
        return "unknown";
    }
}
