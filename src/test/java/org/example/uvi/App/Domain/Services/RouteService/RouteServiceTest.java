package org.example.uvi.App.Domain.Services.RouteService;

import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;
import org.example.uvi.App.Domain.Repository.WaysRepository.WaysRepository;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto.RouteDto;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto.RoutePointDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private WaysRepository waysRepository;

    @InjectMocks
    private RouteService routeService;

    @Test
    void calculateRoute_WhenValidPoints_ReturnsRouteWithCorrectDistance() {
        double startLat = 56.8389, startLon = 60.6057;
        double endLat = 56.8361, endLon = 60.6146;
        RouteMode mode = RouteMode.DRIVING;

        when(waysRepository.findNearestVertex(startLat, startLon, mode)).thenReturn(1L);
        when(waysRepository.findNearestVertex(endLat, endLon, mode)).thenReturn(2L);

        // [seq, node, edge, cost, lon, lat, point_idx]
        // Имитируем один сегмент длиной 500 метров
        Object[] row1 = {0, 1L, 10L, 500.0, 60.6060, 56.8390, 1};
        Object[] row2 = {1, 2L, -1L, 0.0, 60.6140, 56.8360, 0};
        
        when(waysRepository.findRoute(1L, 2L, mode)).thenReturn(List.of(row1, row2));

        RouteDto result = routeService.calculateRoute(startLat, startLon, endLat, endLon, mode);

        assertNotNull(result);
        assertTrue(result.totalDistanceMeters() > 500.0, "Total distance should include last-mile");
        assertEquals(4, result.points().size(), "Should have start, 2 route points, and end");
    }

    @Test
    void calculateRoute_WhenVertexNotFound_ThrowsException() {
        when(waysRepository.findNearestVertex(anyDouble(), anyDouble(), any())).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> 
            routeService.calculateRoute(0, 0, 1, 1, RouteMode.PEDESTRIAN));
    }
}
