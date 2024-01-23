package practice.test.hotel.handler;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.Command;
import practice.test.hotel.entity.Flag;
import practice.test.hotel.service.CustomerService;
import practice.test.hotel.service.InLineKeyboard;
import practice.test.hotel.service.TelegramSenderService;
import practice.test.hotel.util.FlagInput;


import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class CommandHandler {
    // Хранилище для команд (добавление новых команд через конструктор + enum Command)
    private final Map<Command, BiConsumer<User, Message>> commandExecute = new HashMap<>();

    private final CallBackQueryHandler callBackQueryHandler;
    private final TelegramSenderService telegramSenderService;
    private final CustomerService customerService;
    private final InLineKeyboard inLineKeyboard;

    private final FlagInput flagInput;
    private final ManualInputHandler manualInputHandler;
    private final static Logger LOGGER = LoggerFactory.getLogger(CallBackQueryHandler.class);


    public CommandHandler(CallBackQueryHandler callBackQueryHandler, ManualInputHandler manualInputHandler, TelegramSenderService telegramSenderService, CustomerService customerService, InLineKeyboard inLineKeyboard, FlagInput flagInput, ManualInputHandler manualInputHandler1) {
        this.callBackQueryHandler = callBackQueryHandler;
        this.telegramSenderService = telegramSenderService;
        this.customerService = customerService;
        this.inLineKeyboard = inLineKeyboard;
        this.flagInput = flagInput;
        this.manualInputHandler = manualInputHandler1;


        commandExecute.put(Command.START, this::handleStart); // Добавление команд в хранилище (новые делать по примеру)
        commandExecute.put(Command.CANCEL, this::handleCancel);
    }

    /**
     * Обрабатывает все команды который использует бот
     *
     * @param user
     * @param message
     */
    // обработчик команд
    public void handler(User user, Message message) {
        Command[] commands = Command.values();
        for (Command command : commands) {
            if (("/" + command.name().toLowerCase()).equals(message.text())) {
                commandExecute.get(command).accept(user, message);
                break;
            }
        }
    }

    private void handleStart(User user, Message message) {
        LOGGER.info("Was invoked method -> /start");
        customerService.registerCustomer(user.id());
    }

    private void handleCancel(User user, Message message) {
        LOGGER.info("Was invoked method -> /cancel");
        if (flagInput.flag() != Flag.None && flagInput.flag() != Flag.Customer) {
            flagInput.flagSet(Flag.None);
            manualInputHandler.setDataIn(null);
            inLineKeyboard.startMenu(user.id());
        }
        else {
            telegramSenderService.send(user.id(), "В данный момент вы ничего не вводите");
        }

    }

}


