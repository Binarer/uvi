package org.example.uvi.App.Domain.Enums.PlaceType;

import lombok.Getter;

@Getter
public enum PlaceType {
    RESTAURANT("Ресторан"),
    CAFE("Кафе"),
    BAR("Бар"),
    CLUB("Клуб"),
    CONCERT("Концерт"),
    FESTIVAL("Фестиваль"),
    EXHIBITION("Выставка"),
    THEATER("Театр"),
    CINEMA("Кинотеатр"),
    MUSEUM("Музей"),
    PARK("Парк"),
    SPORTS("Спортивное мероприятие"),
    CONFERENCE("Конференция"),
    WORKSHOP("Мастер-класс"),
    PARTY("Вечеринка"),
    SIGHT("Достопримечательность"),
    SHOPPING("Торговый центр"),
    STREET("Улица"),
    SQUARE("Площадь"),
    OTHER("Другое");

    private final String displayName;

    PlaceType(String displayName) {
        this.displayName = displayName;
    }
}
