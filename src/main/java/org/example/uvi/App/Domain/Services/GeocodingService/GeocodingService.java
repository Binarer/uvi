package org.example.uvi.App.Domain.Services.GeocodingService;

import lombok.RequiredArgsConstructor;
import org.example.uvi.App.Domain.Repository.PlaceRepository.PlaceRepository;
import org.example.uvi.App.Domain.Repository.WaysRepository.WaysRepository;
import org.example.uvi.App.Infrastructure.Http.Dto.GeocodingDto.GeocodingResultDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final PlaceRepository placeRepository;
    private final WaysRepository waysRepository;

    public List<GeocodingResultDto> search(String query) {
        List<GeocodingResultDto> results = new ArrayList<>();

        // 1. Поиск по нашим местам (POIs)
        placeRepository.searchPlaces(query).forEach(place -> {
            results.add(new GeocodingResultDto(
                    place.getName(),
                    place.getAddress(),
                    place.getLatitude(),
                    place.getLongitude(),
                    "PLACE"
            ));
        });

        // 2. Поиск по улицам из OSM
        waysRepository.searchStreets(query).forEach(row -> {
            results.add(new GeocodingResultDto(
                    (String) row[0],
                    "Улица, Екатеринбург",
                    (Double) row[1],
                    (Double) row[2],
                    "STREET"
            ));
        });

        return results;
    }
}
