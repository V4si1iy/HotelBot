package practice.test.hotel.service;

import com.pengrad.telegrambot.model.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.Category;
import practice.test.hotel.model.Booking;
import practice.test.hotel.model.Customer;
import practice.test.hotel.model.HotelRoom;
import practice.test.hotel.repository.CustomerRepository;
import practice.test.hotel.repository.HotelRoomRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotelRoomService {
    private final HotelRoomRepository hotelRoomRepository;
    private final CustomerRepository customerRepository;


    public HotelRoomService(HotelRoomRepository hotelRoomRepository, CustomerRepository customerRepository) {
        this.hotelRoomRepository = hotelRoomRepository;
        this.customerRepository = customerRepository;

    }

    /**
     * Метод ищет все номера в базе данных
     *
     * @return найденные номера
     * @see HotelRoomRepository#findAll()
     */
    @Cacheable("rooms")
    public List<HotelRoom> findAll() {
        return hotelRoomRepository.findAll();
    }

    //    @Cacheable("rooms")
//    public HotelRoom findByName(String name) {
//        return hotelRoomRepository.findByName(name);
//    }
    @Cacheable("rooms")
    public HotelRoom findById(Long id) {
        return hotelRoomRepository.findById(id).orElse(null);
    }

    /**
     * Метод добавляет номер в базу данных
     *
     * @param room - номер, который надо добавить
     * @return добавленный номер
     */
    @CachePut("rooms")
    public HotelRoom create(HotelRoom room) {
        return hotelRoomRepository.save(room);
    }

    @CachePut("rooms")
    public HotelRoom update(HotelRoom room) {
        return (findById(room.getId()) != null) ? hotelRoomRepository.save(room) : null;
    }

    @CacheEvict("rooms")
    public HotelRoom delete(Long id) {
        HotelRoom room = findById(id);
        if (room != null) {
            hotelRoomRepository.delete(room);
        }
        return room;
    }

    public void checkRooms() {
        List<HotelRoom> rooms = findAll();
        List<Booking> temporaryRoom;
        for (HotelRoom room : rooms) {
            temporaryRoom = room.getBooking().parallelStream()
                    .filter(booking -> !Objects.isNull(booking.getBookingOut()) && booking.getBookingOut().isBefore((LocalDate.now()))).toList(); //проверка на налачие даты выезда и окончание срока даты выезда
            temporaryRoom.stream().forEach(booking ->
            {
                booking.setBookingOut(null);
                booking.setBookingIn(null);
                booking.setCustomer(null);
            });
            update(room);
        }

    }

    public String outputBooked(Long chatId) {
        Customer customer = customerRepository.findByChatId(chatId);
        checkRooms();

        Map<Category, List<HotelRoom>> groupRooms = findAll().parallelStream()
                .filter(room -> room.getBooking().size() != 0) // фильтруем номера у которых есть бронирование
                .map(room -> {
                    List<Booking> filteredBookings = room.getBooking().stream()
                            .filter(checkCustomer -> checkCustomer.getCustomer().equals(customer))
                            .collect(Collectors.toList());
                    room.setBooking(filteredBookings); // Устанавливаем отфильтрованные бронирования обратно в номер
                    return room;
                })
                .filter(room -> room.getBooking().size() != 0) // убираем пустые оставшиеся лишние номера
                .collect(Collectors.groupingBy(HotelRoom::getCategory));

        String str = "Информация о бронировании:\n\n";

        for (Category category : Category.values()) {
            if (groupRooms.isEmpty()) {
                str += "У вас нет забронированных номеров.\n\n";
                break;
            }

            if (Objects.isNull(groupRooms.get(category)))
                continue;
            str += "Категория номера: " + category + "\n" + "Забронированные номера:\n\n";


            for (HotelRoom room : groupRooms.get(category)) {
                str += "Номер " + room.getId() + ":\n";
                for (Booking booking : room.getBooking()) {
                    str += "Дата заезда: " + booking.getBookingIn() + "\n" +
                            "Дата выезда: " + booking.getBookingOut() + "\n\n";
                }
            }

        }
        str += "Дополнительная информация:\n" +
                "\n" +
                "Все номера предоставляют бесплатный Wi-Fi, кондиционер, телевизор и телефон.\n" +
                "Завтрак включен в стоимость проживания.\n" +
                "Гости могут воспользоваться услугами тренажерного зала и бассейна.\n\n" +
                "Контактная информация:\n" +
                "\n" +
                "Телефон для справок: 8-800-555-35-35\n" +
                "Электронная почта: example@gmail.com\n\n" +
                "Полезная информация:\n" +
                "\n" +
                "Регистрация заезда с 14:00. Регистрация выезда до 12:00.\n";
        return str;
    }


}
