package org.example.uvi.App.Domain.Services.GeocodingService;

import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Repository.PlaceRepository.PlaceRepository;
import org.example.uvi.App.Domain.Repository.WaysRepository.WaysRepository;
import org.example.uvi.App.Infrastructure.Http.Dto.GeocodingDto.GeocodingResultDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private WaysRepository waysRepository;

    @InjectMocks
    private GeocodingService geocodingService;

    @Test
    void search_WhenQueryMatchesPlaceAndStreet_ReturnsBoth() {
        String query = "Lenina";
        
        Place place = Place.builder()
                .name("Cafe Lenina")
                .address("Lenina street, 1")
                .latitude(56.1)
                .longitude(60.1)
                .build();

        when(placeRepository.searchPlaces(query)).thenReturn(List.of(place));
        List<Object[]> streets = new java.util.ArrayList<>();
        streets.add(new Object[]{"Lenina St", 56.2, 60.2});
        when(waysRepository.searchStreets(query)).thenReturn(streets);

        List<GeocodingResultDto> results = geocodingService.search(query);

        assertEquals(2, results.size());
        
        assertTrue(results.stream().anyMatch(r -> r.type().equals("PLACE") && r.name().equals("Cafe Lenina")));
        assertTrue(results.stream().anyMatch(r -> r.type().equals("STREET") && r.name().equals("Lenina St")));
    }

    @Test
    void search_WhenNoMatches_ReturnsEmptyList() {
        String query = "Unknown";
        when(placeRepository.searchPlaces(query)).thenReturn(List.of());
        when(waysRepository.searchStreets(query)).thenReturn(List.of());

        List<GeocodingResultDto> results = geocodingService.search(query);

        assertTrue(results.isEmpty());
    }
}
