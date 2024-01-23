package practice.test.hotel.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;
import practice.test.hotel.entity.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class HotelRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Category category;
    @OneToMany(fetch = FetchType.EAGER)
    private List<Booking> booking;

    public void addBooking(Booking booking) {
        this.booking.add(booking);
    }
    public void deleteBooking(Booking booking)
    {
        this.booking.remove(booking);
    }
}
