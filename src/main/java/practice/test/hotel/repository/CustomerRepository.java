package practice.test.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import practice.test.hotel.model.Customer;


@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer findByChatId(Long chatId);
}
