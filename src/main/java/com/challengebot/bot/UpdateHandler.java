package com.challengebot.bot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UpdateHandler implements UpdatesListener {
    @Override
    public int process(List<Update> updates) {
        // TODO: parse commands and callback data
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
