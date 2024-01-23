package practice.test.hotel.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CallBackData // перечисление кнопок в боте
{
    BOOKED("Забронированые номера"),
    BOOKING("Бронирование номера"),
    BACK_MAIN_MENU("Вернуться к главному меню"),
    BOOKING_ROOM_YES("Забронировать номер"),
    BACK_BOOKING("Вернуться к выбору категории"),
    CANCEL_RESERVATION("Отменить бронирование"),
    BACK_BOOKED("Вернуться к забронированым номерам");


    private final String name;


    public final String getName() {
        return name;
    }

    /**
     * парсер для поиска команда в enum
     *
     * @param data данные кнопки нажатой в боте
     * @return callBackData - возвращает найденную кнопку, если ее нет - null
     */
    public static CallBackData parse(String data) {
        for (CallBackData callBackData : CallBackData.values()) {
            if (callBackData.toString().equals(data)) {
                return callBackData;
            }
        }
        return null;
    }
}
