package practice.test.hotel.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.springframework.stereotype.Service;

@Service
public class TelegramSenderService {
    private final TelegramBot telegramBot;

    public TelegramSenderService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }
    public void send(long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse response = telegramBot.execute(sendMessage);
    }
}
