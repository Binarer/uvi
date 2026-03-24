package org.example.uvi.App.Infrastructure.Http.Controller.RouteController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.RouteService.RouteService;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto.RouteDto;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto.RouteRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Route calculation using OpenStreetMap road network (Yekaterinburg)")
@SecurityRequirement(name = "bearerAuth")
public class RouteController {

    private final RouteService routeService;

    @PostMapping("/calculate")
    @Operation(
            summary = "Calculate route between two points",
            description = """
                    Builds an optimal route along the road network of Yekaterinburg
                    using pgRouting (Dijkstra algorithm) and OSM data.

                    **Modes:**
                    - `DRIVING` — personal vehicle: all roads except pedestrian paths.
                    - `PEDESTRIAN` — walking: footways, paths, steps, living streets.
                    - `PUBLIC_TRANSPORT` — public transport: bus/tram/trolleybus roads.

                    Returns ordered list of coordinates, total distance in meters and kilometers.
                    """
    )
    public ResponseEntity<RouteDto> calculateRoute(
            @Valid @RequestBody RouteRequest request) {
        RouteDto route = routeService.calculateRoute(
                request.startLat(), request.startLon(),
                request.endLat(),   request.endLon(),
                request.mode()
        );
        return ResponseEntity.ok(route);
    }
}
