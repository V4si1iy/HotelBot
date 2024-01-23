package practice.test.hotel.handler;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import jakarta.persistence.GeneratedValue;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.CallBackData;
import practice.test.hotel.entity.Category;
import practice.test.hotel.entity.Flag;
import practice.test.hotel.model.Booking;
import practice.test.hotel.model.HotelRoom;
import practice.test.hotel.service.BookingService;
import practice.test.hotel.service.HotelRoomService;
import practice.test.hotel.service.InLineKeyboard;
import practice.test.hotel.util.FlagInput;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;


@Service
public class CallBackQueryHandler {
    // Хранилище для команд (добавление новых команд через конструктор + enum CallBackData)
    private final Map<CallBackData, BiConsumer<User, CallbackQuery>> commandExecute = new HashMap<>();
    private final InLineKeyboard inLineKeyboard;
    private final TelegramBot telegramBot;
    private final FlagInput flagInput;
    private final HotelRoomService hotelRoomService;
    @Getter
    private Category category;
    private final BookingService bookingService;
    @Getter
    private HotelRoom room;

    private final static Logger LOGGER = LoggerFactory.getLogger(CallBackQueryHandler.class);

    public CallBackQueryHandler(InLineKeyboard inLineKeyboard, TelegramBot telegramBot, FlagInput flagInput, HotelRoomService hotelRoomService, BookingService bookingService) {
        // пример добавления команды: commandExecute.put(CallBackData.<Button>, this::handle<Button>);
        this.inLineKeyboard = inLineKeyboard;
        this.telegramBot = telegramBot;
        this.flagInput = flagInput;
        this.hotelRoomService = hotelRoomService;
        this.bookingService = bookingService;


        commandExecute.put(CallBackData.BOOKING, this::handleBooking);
        commandExecute.put(CallBackData.BOOKED, this::handleBooked);
        commandExecute.put(CallBackData.BACK_BOOKING, this::handleBooking);
        commandExecute.put(CallBackData.BACK_MAIN_MENU, this::startMenuCallBack);
        commandExecute.put(CallBackData.BOOKING_ROOM_YES, this::handleBookingRoomYes);
        commandExecute.put(CallBackData.CANCEL_RESERVATION, this::handleCancelReservation);
        commandExecute.put(CallBackData.BACK_BOOKED, this::handleBooked);

    }


    /**
     * Обрабатывает все нажатые кнопки в боте
     *
     * @param callbackQuery
     */
    // открытый обработчик кнопок (вызывать его если надо)
    public void handler(CallbackQuery callbackQuery) {
        User user = callbackQuery.from();
        CallBackData callBackData = CallBackData.parse(callbackQuery.data());
        checkUniqueChoose(user, callBackData, callbackQuery);
        handler(user, callBackData, callbackQuery);
    }

    private void checkUniqueChoose(User user, CallBackData callBackData, CallbackQuery callbackQuery) {
        if (Objects.isNull(callBackData)) {
            Category category = Category.parse(callbackQuery.data());
            HotelRoom room = null;
            if (!Objects.isNull(category)) {  // Если выбор среди категорий
                this.category = category;
                handleAllCategory(callbackQuery, category);
            } else {
                String[] value = callbackQuery.data().split(" ");
                if (value[0].equals("D")) { // Выбор среди дат
                    handleAllHotelRoomDates(callbackQuery, value[1]);
                } else { // Выбор среди номеров
                    handleAllHotelRooms(callbackQuery, value[1]);
                }
            }
        }
    }

    private void handler(User user, CallBackData callBackData, CallbackQuery callbackQuery) {
        CallBackData[] callBackDates = CallBackData.values();
        for (CallBackData data : callBackDates) {
            if (callBackData == data) {
                commandExecute.get(data).accept(user, callbackQuery);
                break;
            }
        }
    }

    private void handleAllCategory(CallbackQuery callbackQuery, Category category) {
        LOGGER.info("Was invoked method to see categories");
        hotelRoomService.checkRooms();
        EditMessageText messageText;
        messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), category.getDescription() + "\n Хотите забронировать?").replyMarkup(inLineKeyboard.inlineBookingChoice());
        telegramBot.execute(messageText);
    }

    private void handleBookingRoomYes(User user, CallbackQuery callbackQuery) {
        LOGGER.info("Was invoked method to book room");
        EditMessageText messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Напишите дату заезда DD.MM.YYYY\n /cancel для отмены");
        telegramBot.execute(messageText);
        flagInput.flagSet(Flag.BOOKING);

    }

    private void handleBooking(User user, CallbackQuery callbackQuery) {
        LOGGER.info("Was invoked method to booking room");
        EditMessageText messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Выберите категорию номера").replyMarkup(inLineKeyboard.allCategories());
        telegramBot.execute(messageText);
    }

    public void startMenuCallBack(User user, CallbackQuery callbackQuery) {
        LOGGER.info("Was invoked method to show start menu");
        DeleteMessage delete = new DeleteMessage(user.id(), callbackQuery.message().messageId());
        telegramBot.execute(delete);
        inLineKeyboard.startMenu(user.id());
    }


    private void handleBooked(User user, CallbackQuery callbackQuery) {
        LOGGER.info("Was invoked method to see booked room");
        EditMessageText messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), hotelRoomService.outputBooked(user.id())).replyMarkup(inLineKeyboard.inlineBookedMenu(user));
        telegramBot.execute(messageText);
    }

    private void handleCancelReservation(User user, CallbackQuery callbackQuery) {
        LOGGER.info("Was invoked method to cancel reservation");
        EditMessageText messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Выберите номер").replyMarkup(inLineKeyboard.hotelRoomsInLineKeyboard(user));
        telegramBot.execute(messageText);
    }

    private void handleAllHotelRooms(CallbackQuery callbackQuery, String roomId) {
        LOGGER.info("Was invoked method to control all rooms click");
        room = hotelRoomService.findById(Long.valueOf(roomId));
        hotelRoomService.checkRooms();
        EditMessageText messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Выберите дату").replyMarkup(inLineKeyboard.hotelRoomDatesInLineKeyboard(callbackQuery.from(), hotelRoomService.findById(Long.valueOf(roomId))));
        telegramBot.execute(messageText);

    }

    private void handleAllHotelRoomDates(CallbackQuery callbackQuery, String dateId) {
        LOGGER.info("Was invoked method to control all dates click");
        Booking booking = bookingService.findById(Long.valueOf(dateId));
        room.deleteBooking(booking);
        hotelRoomService.update(room);
        bookingService.delete(Long.valueOf(dateId));
        EditMessageText messageText = new EditMessageText(callbackQuery.message().chat().id(), callbackQuery.message().messageId(), "Выберите номер").replyMarkup(inLineKeyboard.hotelRoomsInLineKeyboard(callbackQuery.from()));
        telegramBot.execute(messageText);

    }

}
