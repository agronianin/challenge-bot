package su.msk.nlx2.challengebot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat")
public class Chat {
    @Id
    @Column(name = "tg_chat_id", nullable = false)
    private Long tgChatId;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "chat")
    private List<Program> programs = new ArrayList<>();
}
