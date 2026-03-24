package org.example.uvi.App.Infrastructure.Http.Dto.GeocodingDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Geocoding search result (Place or Street)")
public record GeocodingResultDto(
    @Schema(description = "Name of the place or street", example = "Dendrological Park")
    String name,
    @Schema(description = "Physical address", example = "8 Marta St, 37")
    String address,
    @Schema(description = "Latitude coordinate", example = "56.8286")
    Double latitude,
    @Schema(description = "Longitude coordinate", example = "60.6033")
    Double longitude,
    @Schema(description = "Result type: PLACE (from DB) or STREET (from OSM)", example = "PLACE")
    String type // "PLACE" or "STREET"
) {}
