package practice.test.hotel.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.CallBackData;
import practice.test.hotel.entity.Category;
import practice.test.hotel.model.Booking;
import practice.test.hotel.model.HotelRoom;

import java.util.Objects;

@Service
public class InLineKeyboard {
    private InlineKeyboardMarkup inlineKeyboardMarkup;
    private final TelegramBot telegramBot;
    private final HotelRoomService hotelRoomService;

    public InLineKeyboard(TelegramBot telegramBot, HotelRoomService hotelRoomService) {
        this.telegramBot = telegramBot;
        this.hotelRoomService = hotelRoomService;
    }


    public InlineKeyboardMarkup allCategories() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        for (Category category : Category.values()) {
            inlineKeyboardMarkup.addRow(new InlineKeyboardButton(category.name()).callbackData(category.toString()));
        }
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(CallBackData.BACK_MAIN_MENU.getName()).callbackData(CallBackData.BACK_MAIN_MENU.toString()));
        return inlineKeyboardMarkup;
    }


    public void startMenu(Long chatId) {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton[] buttons = {
                new InlineKeyboardButton(CallBackData.BOOKING.getName()).callbackData(CallBackData.BOOKING.toString()),
                new InlineKeyboardButton(CallBackData.BOOKED.getName()).callbackData(CallBackData.BOOKED.toString())
        };
        inlineKeyboardMarkup.addRow(buttons[0]);
        inlineKeyboardMarkup.addRow(buttons[1]);

        SendMessage newMessage = new SendMessage(chatId, "Добро пожаловать в отель \"In2It\" — уютное убежище среди городской суеты. Наш отель предлагает непринужденную атмосферу и стильный дизайн, созданные для того, чтобы каждый гость чувствовал себя особенным.\n" +
                "\n" +
                "Современные номера отеля \"In2It\" оборудованы всем необходимым для комфортного пребывания. Уникальный интерьер, удобная мебель и теплые оттенки создают атмосферу уюта и гармонии. Бесплатный Wi-Fi во всех номерах обеспечивает связь в любой момент.\n" +
                "\n" +
                "Наши гости могут наслаждаться великолепным видом из окон, открывающимся на городской пейзаж. Отель \"In2It\" также гордится своим отличным расположением, обеспечивающим легкий доступ к главным достопримечательностям города.\n" +
                "\n" +
                "Мы стремимся сделать пребывание наших гостей незабываемым. Помимо комфортных номеров, мы предлагаем удивительный сервис, включая круглосуточный ресепшн и возможность заказа завтрака в номер. Отель \"In2It\" — это не просто место для ночлега, это опыт, который оставит в вас приятные воспоминания.").replyMarkup(inlineKeyboardMarkup);
        telegramBot.execute(newMessage);

    }

    public InlineKeyboardMarkup inlineBookingChoice() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton[] buttons = {
                new InlineKeyboardButton(CallBackData.BOOKING_ROOM_YES.getName()).callbackData(CallBackData.BOOKING_ROOM_YES.toString()),
                new InlineKeyboardButton(CallBackData.BACK_BOOKING.getName()).callbackData(CallBackData.BACK_BOOKING.toString())
        };
        inlineKeyboardMarkup.addRow(buttons[0]);
        inlineKeyboardMarkup.addRow(buttons[1]);
        return inlineKeyboardMarkup;

    }

    public InlineKeyboardMarkup inlineBookingOut() {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton[] buttons = {
                new InlineKeyboardButton(CallBackData.BACK_BOOKING.getName()).callbackData(CallBackData.BACK_BOOKING.toString())
        };
        inlineKeyboardMarkup.addRow(buttons[0]);
        return inlineKeyboardMarkup;

    }

    public InlineKeyboardMarkup inlineBookedMenu(User user) {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        if(hotelRoomService.findAll().parallelStream()
                .filter(room -> room.getBooking().size()!=0) // фильтруем номера у которых есть бронирование
                .flatMap(room -> room.getBooking().parallelStream()
                        .filter(checkCustomer -> checkCustomer.getCustomer().getChatId() == user.id()) // фильтруем даты только с нужным нам пользователем
                        .map(booking -> room))
                .filter(room -> room.getBooking().size()!=0).count() != 0)
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(CallBackData.CANCEL_RESERVATION.getName()).callbackData(CallBackData.CANCEL_RESERVATION.toString()));
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(CallBackData.BACK_MAIN_MENU.getName()).callbackData(CallBackData.BACK_MAIN_MENU.toString()));
        return inlineKeyboardMarkup;
    }


    public InlineKeyboardMarkup hotelRoomsInLineKeyboard(User user) {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        hotelRoomService.findAll().stream()
                .filter(room -> room.getBooking().size()!=0) // фильтруем номера у которых есть бронирование
                .flatMap(room -> room.getBooking().parallelStream()
                        .filter(checkCustomer -> checkCustomer.getCustomer().getChatId() == user.id()) // фильтруем даты только с нужным нам пользователем
                        .map(booking -> room))
                .distinct()
                .filter(room -> room.getBooking().size()!=0)
                .forEach(room ->
                inlineKeyboardBuilderRooms(inlineKeyboardMarkup, room)
        );
        inlineKeyboardMarkup.addRow(new InlineKeyboardButton(CallBackData.BACK_BOOKED.getName()).callbackData(CallBackData.BACK_BOOKED.toString()));
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup inlineKeyboardBuilderRooms(InlineKeyboardMarkup inlineKeyboardMarkup, HotelRoom room) {
        return inlineKeyboardMarkup.addRow(new InlineKeyboardButton(room.getCategory()+" Номер: " + room.getId()).callbackData("R "+room.getId()));
    }

    public InlineKeyboardMarkup hotelRoomDatesInLineKeyboard(User user, HotelRoom room) {
        inlineKeyboardMarkup = new InlineKeyboardMarkup();
        room.getBooking().stream()
                .filter(checkCustomer -> checkCustomer.getCustomer().getChatId() == user.id())
                .forEach(booking ->
                inlineKeyboardBuilderRoomDates(inlineKeyboardMarkup, booking)
        );

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup inlineKeyboardBuilderRoomDates(InlineKeyboardMarkup inlineKeyboardMarkup, Booking booking) {
        return inlineKeyboardMarkup.addRow(new InlineKeyboardButton(booking.getBookingIn() + " - " + booking.getBookingOut()).callbackData("D "+booking.getId()));
    }

}
