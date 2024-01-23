package practice.test.hotel.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Category {
    STANDARD("стандарт", "Стандартный номер:\n" +
            "Идеальный выбор для тех, кто ценит комфорт и функциональность. Номер стандартного класса предлагает уютное пространство с современным дизайном, обеспечивая все необходимые удобства для приятного отдыха. В стандартном номере вы найдете удобную мебель, стильный интерьер, телевизор, кондиционер, а также чайник с набором чая и кофе. Бесплатный Wi-Fi доступен во всех номерах, обеспечивая удобное подключение к интернету. Для удобства гостей также предоставляется ежедневная уборка номера. \n" +
            "Стоимость проживания: от 3000 рублей в сутки."),

    VIP("вип", "VIP-номер:\n" +
            "Для гостей, привыкших к высокому уровню обслуживания и роскоши. VIP-номер предлагает роскошное пространство с элегантным декором и уникальными элементами дизайна. Гости могут наслаждаться широким спектром эксклюзивных услуг, включая персональное обслуживание, возможность заказа бесплатного завтрака в номер, доступ к VIP-зоне отеля и консьерж-сервис. Кроме того, в номере предоставляется мини-бар с разнообразным ассортиментом напитков, а также бесплатный Wi-Fi.\n" +
            "Стоимость проживания: от 5000 рублей в сутки."),

    DELUXE("делюкс", "Делюкс-номер:\n" +
            "Идеальный выбор для тех, кто ищет роскошное проживание с дополнительными удобствами. Делюкс-номер предоставляет просторное и стильное пространство, современный дизайн и высококлассные удобства. Гости могут наслаждаться прекрасным видом из окна, уникальным интерьером и дополнительными услугами, такими как бесплатный Wi-Fi, мини-бар с выбором закусок и напитков, а также роскошные туалетные принадлежности высокого качества. Услуги ежедневной уборки и обслуживания номера делают пребывание еще более комфортным. Кроме того, гости имеют доступ к спа-центру отеля, бесплатному трансферу и увеличенному выбору фирменных блюд в ресторане отеля.\n" +
            "Стоимость проживания: от 8000 рублей в сутки.");

    private final String name;

    private final String description;
    public static Category parse(String value) {
        for (Category category : Category.values()) {
            if (category.toString().equals(value)) {
                return category;
            }

        }
        return null;
    }
}