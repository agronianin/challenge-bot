package su.msk.nlx2.challengebot.repository;

import java.time.LocalTime;
import java.util.List;
import su.msk.nlx2.challengebot.model.UserReminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReminderRepository extends JpaRepository<UserReminder, Integer> {
    List<UserReminder> findByUser_IdOrderByRemindTimeAsc(Integer userId);
    boolean existsByUser_IdAndRemindTime(Integer userId, LocalTime remindTime);
}
