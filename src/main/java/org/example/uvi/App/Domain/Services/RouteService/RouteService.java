package org.example.uvi.App.Domain.Services.RouteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;
import org.example.uvi.App.Domain.Repository.WaysRepository.WaysRepository;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto;
import org.example.uvi.App.Infrastructure.Http.Dto.RoutePointDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final WaysRepository waysRepository;

    /**
     * Построить маршрут между двумя точками по дорожной сети Екатеринбурга.
     *
     * @param startLat широта начальной точки
     * @param startLon долгота начальной точки
     * @param endLat   широта конечной точки
     * @param endLon   долгота конечной точки
     * @param mode     режим маршрутизации (PEDESTRIAN / PUBLIC_TRANSPORT / DRIVING)
     * @return RouteDto с точками маршрута, дистанцией в метрах и км
     */
    public RouteDto calculateRoute(double startLat, double startLon,
                                   double endLat, double endLon,
                                   RouteMode mode) {
        log.info("Calculating {} route from ({},{}) to ({},{})",
                mode, startLat, startLon, endLat, endLon);

        // Найти ближайшие вершины графа к заданным точкам с учётом режима
        Long startVertex = waysRepository.findNearestVertex(startLat, startLon, mode);
        Long endVertex   = waysRepository.findNearestVertex(endLat,   endLon,   mode);

        if (startVertex == null || endVertex == null) {
            throw new IllegalArgumentException(
                    "Не удалось найти ближайшие точки дорожной сети для режима " + mode);
        }

        if (startVertex.equals(endVertex)) {
            throw new IllegalArgumentException(
                    "Начальная и конечная точки совпадают или находятся слишком близко");
        }

        log.info("Start vertex: {}, End vertex: {}, Mode: {}", startVertex, endVertex, mode);

        // Получить маршрут через pgr_dijkstra с фильтром по режиму
        List<Object[]> rows = waysRepository.findRoute(startVertex, endVertex, mode);

        if (rows == null || rows.isEmpty()) {
            throw new IllegalStateException(
                    "Маршрут между указанными точками не найден для режима " + mode +
                    ". Убедитесь, что точки находятся в зоне покрытия карты Екатеринбурга.");
        }

        // Преобразовать результат в точки маршрута
        // Столбцы: seq(0), node(1), edge(2), cost(3), lon(4), lat(5), point_idx(6)
        // Дедупликация: убираем подряд идущие одинаковые точки
        List<RoutePointDto> routePoints = new ArrayList<>();
        RoutePointDto prev = null;
        for (Object[] row : rows) {
            double lat = toDouble(row[5]);
            double lon = toDouble(row[4]);
            RoutePointDto pt = new RoutePointDto(lat, lon);
            if (prev == null
                    || Math.abs(prev.latitude()  - lat) > 1e-9
                    || Math.abs(prev.longitude() - lon) > 1e-9) {
                routePoints.add(pt);
                prev = pt;
            }
        }

        // Суммарная дистанция (cost хранит метры в pgRouting с OSM данными)
        double totalDistanceMeters = rows.stream()
                .mapToDouble(row -> toDouble(row[3]))
                .filter(c -> c >= 0)
                .sum();

        double totalDistanceMetersRounded = Math.round(totalDistanceMeters * 10.0) / 10.0;
        double totalDistanceKm = Math.round((totalDistanceMeters / 1000.0) * 1000.0) / 1000.0;

        // Last-mile: прямые линии от/до реальных точек (вход/выход из здания)
        List<RoutePointDto> points = new ArrayList<>();
        points.add(new RoutePointDto(startLat, startLon));
        points.addAll(routePoints);
        points.add(new RoutePointDto(endLat, endLon));

        log.info("Route [{}] found: {} points, {} m / {} km",
                mode, points.size(), totalDistanceMetersRounded, totalDistanceKm);

        return new RouteDto(
                startLat,
                startLon,
                endLat,
                endLon,
                mode,
                totalDistanceMetersRounded,
                totalDistanceKm,
                points.size(),
                points
        );
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number n) return n.doubleValue();
        return Double.parseDouble(value.toString());
    }
}
