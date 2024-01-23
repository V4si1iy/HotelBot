package practice.test.hotel.handler;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.Flag;
import practice.test.hotel.model.Booking;
import practice.test.hotel.model.Customer;
import practice.test.hotel.model.HotelRoom;
import practice.test.hotel.service.*;
import practice.test.hotel.util.FlagInput;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ManualInputHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(ManualInputHandler.class);
    private final Map<Flag, BiConsumer<User, Message>> commandExecute = new HashMap<>();
    private final CustomerService customerService;

    private final CallBackQueryHandler callBackQueryHandler;
    private final FlagInput flagInput;
    private final InLineKeyboard inLineKeyboard;
    @Setter
    private LocalDate dataIn = null;
    private final TelegramSenderService telegramSenderService;

    private final HotelRoomService hotelRoomService;
    private final BookingService bookingService;
    private final Pattern patternDate = Pattern.compile("(0[1-9]|[12]\\d|3[01])\\.(0[1-9]|1[0-2])\\.(\\d{4})");

    public ManualInputHandler(CustomerService customerService, CallBackQueryHandler callBackQueryHandler, FlagInput flagInput, InLineKeyboard inLineKeyboard, TelegramSenderService telegramSenderService, HotelRoomService hotelRoomService, BookingService bookingService) {
        this.customerService = customerService;
        this.callBackQueryHandler = callBackQueryHandler;
        this.flagInput = flagInput;
        this.inLineKeyboard = inLineKeyboard;
        this.telegramSenderService = telegramSenderService;
        this.hotelRoomService = hotelRoomService;
        this.bookingService = bookingService;
        commandExecute.put(Flag.BOOKING, this::bookingHandler);
        commandExecute.put(Flag.Customer, this::reportCustomerStart);
    }

    /**
     * Метод обрабатывает весь пользовательский ввод из телеграм бота
     *
     * @param user
     * @param message
     */
    @Async
    protected void handler(User user, Message message) {
        Flag[] flags = Flag.values();
        for (Flag data : flags) {
            if (flagInput.flag() == data) {
                commandExecute.get(data).accept(user, message);
                break;
            }
        }
    }


    @Async
    protected void reportCustomerStart(User user, Message message) {
        LOGGER.info("Was invoked method to input Customer name and surname by user");
        if (customerService.createCustomerStart(message.chat(), message)) {
            flagInput.flagSet(Flag.None);
            inLineKeyboard.startMenu(user.id());
        }
    }

    @Async
    protected void bookingHandler(User user, Message message) {
        LOGGER.info("Was invoked method to input booking by user");
        Matcher matcher = patternDate.matcher(message.text());
        LocalDate dataOut;
        if (matcher.matches()) { // является ли датой
            if (!Objects.isNull(dataIn)) { // проверка ввода первой даты
                dataOut = LocalDate.parse(matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if (dataOut.isAfter(dataIn) && dataOut.isBefore(LocalDate.now().plusYears(1))) { // проверка на дурака дата вьезда < дата выезда и ограничение брони 1 год
                    Customer customer = customerService.findByChatId(user.id());
                    List<HotelRoom> rooms = hotelRoomService.findAll();
                    rooms = rooms.parallelStream().filter(e -> e.getCategory().equals(callBackQueryHandler.getCategory())).toList();
                    Long countBooking ;
                    HotelRoom neededRoom = null;
                    for (HotelRoom room : rooms) {
                        if (room.getBooking().size()==0) // Если на номер вообще нет брони
                        {
                            neededRoom = room;
                            break;
                        }
                        countBooking = room.getBooking().stream().filter(booked ->
                                        (booked.getBookingIn().isBefore(dataIn) && booked.getBookingOut().isAfter(dataOut)) ||  //наша дата внутри границ проверяемой даты
                                                (booked.getBookingIn().isAfter(dataIn) && booked.getBookingOut().isBefore(dataOut)) || // проверяемая дата находится внутри гранц нашей даты
                                                (booked.getBookingIn().isBefore(dataOut) && booked.getBookingOut().isAfter(dataOut)) || //наша дата выезда находтся внутри проверяемой даты
                                                (booked.getBookingIn().isBefore(dataIn) && booked.getBookingOut().isAfter(dataIn)) || // наша дата вьезда находится внутри проверяемой даты
                                                (booked.getBookingIn().isEqual(dataIn) || booked.getBookingOut().isEqual(dataOut)))  // наша дата выезда или вьезда совпадает с проверяемыми датами
                                .count();

                        if (countBooking == 0) {
                            neededRoom=room;
                            }
                    }

                    if(Objects.isNull(neededRoom)){ // После фильтрации нашлись номера с пересекающимся датами
                    telegramSenderService.send(user.id(), "Мест нет на данную дату.");
                    dataIn = null;
                    return;
                }
                    Booking newBooking = new Booking();
                    newBooking.setBookingIn(dataIn);
                    newBooking.setBookingOut(dataOut);
                    newBooking.setCustomer(customer);
                    neededRoom.addBooking(newBooking);
                    bookingService.create(newBooking); // Создание брони и добавление ее в базу данных
                    hotelRoomService.update(neededRoom);
                    dataIn = null;
                    flagInput.flagSet(Flag.None);
                    telegramSenderService.send(user.id(), "Вы успешно забронировали номер: " + neededRoom.getCategory() + "\nДата заезда: " + newBooking.getBookingIn() + "\nДата выезда: " + newBooking.getBookingOut() + "\nДля дополнительной информации смотрите \"забронированые номера\"");
                    inLineKeyboard.startMenu(user.id());
                } else {
                    telegramSenderService.send(user.id(), "Неправильный ввод, введите дату снова.");
                }
            } else {

                dataIn = LocalDate.parse(matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                if (dataIn.isBefore(LocalDate.now().plusDays(1))) { // ограничение бронирования на нужное количество дней от сегодняшнего
                    dataIn = null;
                    telegramSenderService.send(user.id(), "Неправильный ввод, введите дату снова.");
                } else telegramSenderService.send(user.id(), "Введите дату выезда DD.MM.YYYY");
            }


        } else {
            telegramSenderService.send(user.id(), "Неправильный ввод, введите дату снова.");

        }


    }

}
