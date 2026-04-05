package su.msk.nlx2.challengebot.service;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.shared.SharedUser;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import su.msk.nlx2.challengebot.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<su.msk.nlx2.challengebot.model.User> findByTgId(Long tgId) {
        return userRepository.findByTgId(tgId);
    }

    public boolean isAdmin(Long tgId) {
        return userRepository.findByTgId(tgId)
                .map(user -> "admin".equalsIgnoreCase(user.getRole()))
                .orElse(false);
    }

    public su.msk.nlx2.challengebot.model.User syncFromMessage(Message message) {
        User from = message.from();
        if (from == null) {
            throw new IllegalArgumentException("Message sender is missing");
        }

        return userRepository.findByTgId(from.id())
                .map(existing -> updateFromTelegram(existing, from))
                .orElseGet(() -> createFromTelegram(from));
    }

    public su.msk.nlx2.challengebot.model.User promoteToAdmin(SharedUser sharedUser) {
        return userRepository.findByTgId(sharedUser.userId())
                .map(existing -> updatePromotedUser(existing, sharedUser))
                .orElseGet(() -> createPromotedUser(sharedUser));
    }

    private su.msk.nlx2.challengebot.model.User createFromTelegram(User from) {
        su.msk.nlx2.challengebot.model.User user = new su.msk.nlx2.challengebot.model.User();
        user.setTgId(from.id());
        user.setName(buildDisplayName(from.firstName(), from.lastName(), from.username()));
        return userRepository.save(user);
    }

    private su.msk.nlx2.challengebot.model.User updateFromTelegram(
            su.msk.nlx2.challengebot.model.User existing,
            User from
    ) {
        existing.setName(buildDisplayName(from.firstName(), from.lastName(), from.username()));
        return userRepository.save(existing);
    }

    private su.msk.nlx2.challengebot.model.User createPromotedUser(SharedUser sharedUser) {
        su.msk.nlx2.challengebot.model.User user = new su.msk.nlx2.challengebot.model.User();
        user.setTgId(sharedUser.userId());
        user.setName(buildDisplayName(sharedUser.firstName(), sharedUser.lastName(), sharedUser.username()));
        user.setRole("admin");
        return userRepository.save(user);
    }

    private su.msk.nlx2.challengebot.model.User updatePromotedUser(
            su.msk.nlx2.challengebot.model.User existing,
            SharedUser sharedUser
    ) {
        existing.setName(buildDisplayName(sharedUser.firstName(), sharedUser.lastName(), sharedUser.username()));
        existing.setRole("admin");
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
