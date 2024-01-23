package practice.test.hotel.service;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import practice.test.hotel.model.Booking;
import practice.test.hotel.model.HotelRoom;
import practice.test.hotel.repository.BookingRepository;

@Service
@AllArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final HotelRoomService hotelRoomService;

    @Cacheable("booking")
    public Booking findById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    /**
     * Метод добавляет бронирование в базу данных
     *
     * @param booking - бронирование, который надо добавить
     * @return добавленный номер
     */

    @CachePut("booking")
    public Booking create(Booking booking) {
        return bookingRepository.save(booking);
    }

    @CachePut("booking")
    public Booking update(Booking booking) {
        return (findById(booking.getId()) != null) ? bookingRepository.save(booking) : null;
    }

    @CacheEvict("booking")
    public Booking delete(Long id) {
        Booking booking = findById(id);

        if (booking != null) {
            bookingRepository.delete(booking);
        }
        return booking;
    }

}
