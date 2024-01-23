package practice.test.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.test.hotel.model.HotelRoom;

import java.util.List;
import java.util.Optional;
@Repository
public interface HotelRoomRepository extends JpaRepository <HotelRoom, Long> {
    Optional<HotelRoom> findById(Long id);

    HotelRoom save(HotelRoom room);

    void deleteById(Long id);

}
