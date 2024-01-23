package practice.test.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.test.hotel.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {

}
