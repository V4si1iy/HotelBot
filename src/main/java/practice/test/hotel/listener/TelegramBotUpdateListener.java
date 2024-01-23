package practice.test.hotel.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import practice.test.hotel.handler.UpdateHandler;
import practice.test.hotel.service.InLineKeyboard;


import java.util.List;

@Service
@AllArgsConstructor
public class TelegramBotUpdateListener implements UpdatesListener {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdateListener.class);


    private final TelegramBot telegramBot;
    private final UpdateHandler updateHandler;
    private final InLineKeyboard inLineKeyboard;


    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            updateHandler.handler(update);

        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}







