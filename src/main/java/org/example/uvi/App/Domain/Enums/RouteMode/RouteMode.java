package org.example.uvi.App.Domain.Enums.RouteMode;

/**
 * Режим маршрутизации.
 *
 * PEDESTRIAN      — пешеходный маршрут: тротуары, дорожки, пешеходные зоны.
 * PUBLIC_TRANSPORT — общественный транспорт: дороги для автобусов, трамваев, троллейбусов.
 * DRIVING         — личный автомобиль: все дороги кроме пешеходных.
 */
public enum RouteMode {
    PEDESTRIAN,
    PUBLIC_TRANSPORT,
    DRIVING
}
