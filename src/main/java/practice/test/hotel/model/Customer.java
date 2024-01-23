package practice.test.hotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

// Таблица: Пользователь (Customer) в БД
@Entity
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // уникальный id
    private long chatId; // id Telegram чата
    private String surname; // фамилия
    private String name; // имя
    private String phone; // тлф формата +7 или 8



    public Customer() {

    }

    public Customer(long chatId, String surname, String name, String phone) {
        this.chatId = chatId;
        this.surname = surname;
        this.name = name;
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return id == customer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
