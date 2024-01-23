package practice.test.hotel.service;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import practice.test.hotel.entity.Flag;
import practice.test.hotel.handler.CallBackQueryHandler;
import practice.test.hotel.model.Customer;
import practice.test.hotel.repository.CustomerRepository;
import practice.test.hotel.util.FlagInput;


import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private TelegramSenderService telegramSenderService;
    private final CallBackQueryHandler callBackQueryHandler;
    private final FlagInput flagInput;
    private final InLineKeyboard inLineKeyboard;
    private final Pattern patternCustomer = Pattern.compile("(\\+7|8)(\\d{10})");
    private String str1 = null;
    private String str2 =null;

    public CustomerService(CustomerRepository customerRepository, TelegramSenderService telegramSenderService, CallBackQueryHandler callBackQueryHandler, FlagInput flagInput, InLineKeyboard inLineKeyboard) {

        this.customerRepository = customerRepository;
        this.telegramSenderService = telegramSenderService;
        this.callBackQueryHandler = callBackQueryHandler;
        this.flagInput = flagInput;
        this.inLineKeyboard = inLineKeyboard;
    }

    public Collection<Customer> findAll() {

        return customerRepository.findAll();
    }

    public Customer findById(Long id) {

        return customerRepository.findById(id).orElse(null);
    }

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer update(Customer customer) {
        return (findById(customer.getId()) != null) ? customerRepository.save(customer) : null;
    }

    public Customer delete(Long id) {
        Customer customer = findById(id);
        if (customer == null) {
            return null;
        } else {
            customerRepository.deleteById(id);
            return customer;
        }
    }

    public Customer findByChatId(Long id) {

        return customerRepository.findByChatId(id);
    }

    public boolean customerIsExist(Long chatId) {
        Customer customer = findByChatId(chatId);
        if (!Objects.isNull(customer)) {
            return true;
        } else return false;
    }

    public void registerCustomer(Long chatId) {
        if (customerIsExist(chatId)) {
            telegramSenderService.send(chatId, "Здравствуйте! " + findByChatId(chatId).getName() + " Давно Вас не видели)");
            inLineKeyboard.startMenu(chatId);
        } else {
            telegramSenderService.send(chatId, "Здраствуйте! Вы новый пользователь. Для начала работы бота вам нужно ввести ваши персональные данные\nВведите свое имя");
            flagInput.flagSet(Flag.Customer);
        }
    }

    public boolean createCustomerStart(Chat chat, Message message) {
        if (message.text() == null || message.text().isEmpty()) {
            telegramSenderService.send(chat.id(), "Неверный ввод, введите анкету снова");
            return false;
        }
        String str3;
        if (Objects.isNull(str1)) {
            str1 = message.text();
            if (!StringUtils.isBlank(str1)) {
                telegramSenderService.send(chat.id(), "Введите свою фамилию");
            } else {
                telegramSenderService.send(chat.id(), "Неверный ввод, введите имя снова");
                str1 = null;
                return false;
            }
        } else if (Objects.isNull(str2)) {
            str2 = message.text();
            if (!StringUtils.isBlank(str2)) {
                telegramSenderService.send(chat.id(), "Введите свой номер телефона используя +7 или 8");
            } else {
                telegramSenderService.send(chat.id(), "Неверный ввод, введите фамилию снова");
                str2 = null;
                return false;
            }
        } else {
            Matcher matcher = patternCustomer.matcher(message.text());
            if (matcher.matches()) {
                str3= message.text();
                telegramSenderService.send(chat.id(), "Вы прошли регистрацию, спасибо!");
                create(new Customer(chat.id(), str2, str1, str3));
                str1=null;
                str2=null;
                return true;
            } else {
                telegramSenderService.send(chat.id(), "Неверный ввод, введите телефон снова");
                return false;
            }
        }
        return false;
    }
}

