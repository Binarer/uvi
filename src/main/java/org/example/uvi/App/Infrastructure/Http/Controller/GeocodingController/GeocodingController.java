package org.example.uvi.App.Infrastructure.Http.Controller.GeocodingController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Services.GeocodingService.GeocodingService;
import org.example.uvi.App.Infrastructure.Http.Dto.GeocodingDto.GeocodingResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/geocoding")
@RequiredArgsConstructor
@Tag(name = "Geocoding", description = "Search coordinates by name or address")
@SecurityRequirement(name = "bearerAuth")
public class GeocodingController {

    private final GeocodingService geocodingService;

    @GetMapping("/search")
    @Operation(
            summary = "Search for a point (coordinates) by string",
            description = "Performs a dual search: first in the local places database, then in the OSM road network (streets/ways)."
    )
    public ResponseEntity<List<GeocodingResultDto>> search(
            @Parameter(description = "Search query (name, address, or street)", example = "8 Марта")
            @RequestParam String query) {
        return ResponseEntity.ok(geocodingService.search(query));
    }
}
