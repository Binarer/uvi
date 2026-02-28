package org.example.uvi.App.Domain.Repository.WaysRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с таблицами ways и ways_vertices_pgr (pgRouting).
 * Использует нативные SQL запросы через EntityManager.
 *
 * Данные загружены из OSM PBF через osm2pgrouting:
 *   - ways: рёбра графа (201 786 дорог)
 *   - ways_vertices_pgr: вершины графа (149 553 узла)
 */
@Repository
@Slf4j
public class WaysRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Найти ближайшую вершину графа к заданной точке (lat, lon)
     * среди вершин, которые реально входят в подграф для данного режима.
     * Это критично — вершина должна быть связана с рёбрами нужного типа,
     * иначе pgr_dijkstra вернёт пустой результат.
     */
    public Long findNearestVertex(double lat, double lon, RouteMode mode) {
        String tagFilter = switch (mode) {
            case PEDESTRIAN ->
                "tag_value IN ('footway','pedestrian','path','steps','bridleway'," +
                "'living_street','residential','service','unclassified','track','road')";
            case PUBLIC_TRANSPORT ->
                "tag_value IN ('primary','primary_link','secondary','secondary_link'," +
                "'tertiary','tertiary_link','trunk','trunk_link'," +
                "'residential','service','unclassified','living_street','bus_guideway','road','roundabout')";
            case DRIVING ->
                "tag_value NOT IN ('footway','pedestrian','path','steps','cycleway','bridleway')";
        };

        String sql =
            "SELECT v.id FROM ways_vertices_pgr v " +
            "WHERE EXISTS (" +
            "  SELECT 1 FROM ways w " +
            "  WHERE (w.source = v.id OR w.target = v.id) " +
            "    AND w.tag_id IN (SELECT tag_id FROM configuration WHERE " + tagFilter + ")" +
            ") " +
            "ORDER BY v.the_geom <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) " +
            "LIMIT 1";

        List<?> results = em.createNativeQuery(sql)
                .setParameter("lon", lon)
                .setParameter("lat", lat)
                .getResultList();

        if (results == null || results.isEmpty()) return null;
        return ((Number) results.get(0)).longValue();
    }

    /**
     * SQL-фильтр дорог для pgr_dijkstra в зависимости от режима.
     * Строки намеренно однострочные — они вставляются внутрь строкового
     * литерала pgr_dijkstra(''...''), где одинарные кавычки удваиваются.
     *
     * PEDESTRIAN:       пешеходные дорожки, тропы, ступени, жилые улицы.
     *                   Двустороннее движение (reverse_cost = cost).
     * PUBLIC_TRANSPORT: дороги общественного транспорта.
     *                   Учитываем одностороннее движение (reverse_cost).
     * DRIVING:          все дороги кроме сугубо пешеходных.
     *                   Учитываем одностороннее движение (reverse_cost).
     */
    private String buildEdgeSql(RouteMode mode) {
        return switch (mode) {
            case PEDESTRIAN ->
                // Пешеходы: тропы, дорожки, ступени, жилые улицы, грунтовки.
                // Двустороннее движение — reverse_cost = cost.
                // configuration.tag_id — это PK таблицы (не id!)
                "SELECT w.gid AS id, w.source, w.target, w.cost AS cost, w.cost AS reverse_cost " +
                "FROM ways w " +
                "WHERE w.tag_id IN (" +
                "  SELECT tag_id FROM configuration WHERE tag_value IN (" +
                "  ''footway'',''pedestrian'',''path'',''steps'',''bridleway''," +
                "  ''living_street'',''residential'',''service'',''unclassified''," +
                "  ''track'',''road'')" +
                ")";
            case PUBLIC_TRANSPORT ->
                // Общественный транспорт: основные дороги + съезды + развязки.
                // Учитываем одностороннее движение через reverse_cost.
                "SELECT w.gid AS id, w.source, w.target, w.cost AS cost, w.reverse_cost AS reverse_cost " +
                "FROM ways w " +
                "WHERE w.tag_id IN (" +
                "  SELECT tag_id FROM configuration WHERE tag_value IN (" +
                "  ''primary'',''primary_link'',''secondary'',''secondary_link''," +
                "  ''tertiary'',''tertiary_link'',''trunk'',''trunk_link''," +
                "  ''residential'',''service'',''unclassified'',''living_street''," +
                "  ''bus_guideway'',''road'',''roundabout'')" +
                ")";
            case DRIVING ->
                // Авто: все дороги кроме сугубо пешеходных/велосипедных.
                // Включает motorway, trunk, primary и все их link-варианты.
                "SELECT w.gid AS id, w.source, w.target, w.cost AS cost, w.reverse_cost AS reverse_cost " +
                "FROM ways w " +
                "WHERE w.tag_id NOT IN (" +
                "  SELECT tag_id FROM configuration WHERE tag_value IN (" +
                "  ''footway'',''pedestrian'',''path'',''steps'',''cycleway'',''bridleway'')" +
                ")";
        };
    }

    /**
     * Построить маршрут между двумя вершинами через pgr_dijkstra.
     * Возвращает строки: [seq, node, edge, cost, lon, lat, point_idx]
     *
     * Для каждого ребра маршрута разворачиваем полную геометрию (ST_DumpPoints),
     * чтобы получить все промежуточные точки изгиба дороги из OSM.
     * Если ребро проходится в обратном направлении, геометрия реверсируется через ST_Reverse.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findRoute(long startVertex, long endVertex, RouteMode mode) {
        String edgeSql = buildEdgeSql(mode); // кавычки уже удвоены внутри buildEdgeSql

        // Собираем SQL строкой — text blocks нельзя смешивать с динамическим edgeSql
        String query =
            "WITH route AS (" +
            "  SELECT path.seq, path.node, path.edge, path.cost" +
            "  FROM pgr_dijkstra('" + edgeSql + "', :startVertex, :endVertex, true) AS path" +
            ")" +
            " SELECT" +
            "   route.seq, route.node, route.edge, route.cost," +
            "   ST_X((dp).geom) AS lon," +
            "   ST_Y((dp).geom) AS lat," +
            "   (dp).path[1] AS point_idx" +
            " FROM route" +
            " JOIN ways w ON w.gid = route.edge" +
            " CROSS JOIN LATERAL ST_DumpPoints(" +
            "   CASE WHEN route.node = w.source THEN w.the_geom ELSE ST_Reverse(w.the_geom) END" +
            " ) AS dp" +
            " UNION ALL" +
            " SELECT" +
            "   route.seq, route.node, route.edge, route.cost," +
            "   ST_X(v.the_geom) AS lon," +
            "   ST_Y(v.the_geom) AS lat," +
            "   0 AS point_idx" +
            " FROM route" +
            " JOIN ways_vertices_pgr v ON v.id = route.node" +
            " WHERE route.edge = -1" +
            " ORDER BY seq, point_idx";

        return em.createNativeQuery(query)
                .setParameter("startVertex", startVertex)
                .setParameter("endVertex", endVertex)
                .getResultList();
    }
}
