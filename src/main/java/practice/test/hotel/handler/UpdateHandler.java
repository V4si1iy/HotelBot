package practice.test.hotel.handler;

import com.pengrad.telegrambot.model.Update;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.Flag;
import practice.test.hotel.util.FlagInput;


import java.util.Objects;

@AllArgsConstructor
@Service
public class UpdateHandler {
    private final CallBackQueryHandler callBackQueryHandler;
    private final CommandHandler commandHandler;

    private final ManualInputHandler manualInputHandler;
    private final FlagInput flagInput;

    /**
     * Метод для обработки всех данных полученных от бота (<b> Главный метод иерархии обработчиков </b>)
     *
     * @param update данные изменения бота
     */
    @Async
    public void handler(Update update) {
        if (!Objects.isNull(update.callbackQuery())) // Определяет нажата ли всплывающая кнопка
        {
            callBackQueryHandler.handler(update.callbackQuery());
        } else if (update.message().text() != null && update.message().text().startsWith("/")) // поиск команд через "/"
        {
            commandHandler.handler(update.message().from(), update.message());
        } else if (flagInput.flag() != Flag.None)// проверка на ввод пользователя
        {
            manualInputHandler.handler(update.message().from(), update.message());
        }
    }
}
