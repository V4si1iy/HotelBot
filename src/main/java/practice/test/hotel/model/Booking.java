package practice.test.hotel.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate bookingIn;
    private LocalDate bookingOut;
    @ManyToOne
    private Customer customer;
}

