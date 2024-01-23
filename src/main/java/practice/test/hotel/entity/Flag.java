package practice.test.hotel.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Flag {
    BOOKING("флаг готовности принять пользовательский ввод даты букинга"),
    Customer("флаг готовности принять пользовательский ввод анкеты"),

    None("флаг отсутсвия пользователького ввода");
    private final String description;
}
