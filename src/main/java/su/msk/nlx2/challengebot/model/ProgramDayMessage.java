package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "program_day_message",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_program_day_messages_chat_message", columnNames = {"tg_chat_id", "message_id"})
        }
)
public class ProgramDayMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_day_id", nullable = false)
    private ProgramDay programDay;

    @Column(name = "tg_chat_id", nullable = false)
    private Long tgChatId;

    @Column(name = "message_id", nullable = false)
    private Integer messageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
