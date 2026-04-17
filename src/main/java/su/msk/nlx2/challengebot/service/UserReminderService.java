package su.msk.nlx2.challengebot.service;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import su.msk.nlx2.challengebot.model.TgUser;
import su.msk.nlx2.challengebot.model.UserReminder;
import su.msk.nlx2.challengebot.repository.UserReminderRepository;

@Service
@RequiredArgsConstructor
public class UserReminderService {
    private final UserService userService;
    private final UserReminderRepository userReminderRepository;

    public List<UserReminder> findByTgUserId(Long tgUserId) {
        TgUser user = userService.getRequiredByTgId(tgUserId);
        return userReminderRepository.findByUser_IdOrderByRemindTimeAsc(user.getId());
    }

    public boolean addReminder(Long tgUserId, LocalTime remindTime) {
        TgUser user = userService.getRequiredByTgId(tgUserId);
        if (userReminderRepository.existsByUser_IdAndRemindTime(user.getId(), remindTime)) {
            return false;
        }

        UserReminder reminder = new UserReminder();
        reminder.setUser(user);
        reminder.setRemindTime(remindTime);
        userReminderRepository.save(reminder);
        return true;
    }

    public boolean deleteReminder(Long tgUserId, Integer reminderId) {
        TgUser user = userService.getRequiredByTgId(tgUserId);
        return userReminderRepository.findById(reminderId)
                .filter(reminder -> reminder.getUser().getId().equals(user.getId()))
                .map(reminder -> {
                    userReminderRepository.delete(reminder);
                    return true;
                })
                .orElse(false);
    }
}
