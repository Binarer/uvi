package org.example.uvi.App.Domain.Services.PlaceService;

import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Domain.Enums.UserRole.UserRole;
import org.example.uvi.App.Domain.Enums.UserStatus.UserStatus;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Repository.PlaceRepository.PlaceRepository;
import org.example.uvi.App.Domain.Repository.TagRepository.TagRepository;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock private PlaceRepository placeRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserService userService;

    @InjectMocks private PlaceService placeService;

    private User creator;
    private Place testPlace;
    private final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        creator = User.builder()
                .id(1L).firstName("Creator").phoneNumber("79001234567")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();

        testPlace = Place.builder()
                .id(1L).name("Jazz Bar").type(PlaceType.BAR)
                .location(gf.createPoint(new Coordinate(37.6, 55.7)))
                .latitude(55.7).longitude(37.6)
                .isActive(true).createdBy(creator)
                .tags(new HashSet<>()).build();
    }

    @Test
    void getPlaceById_WhenExists_ReturnsPlace() {
        when(placeRepository.findById(1L)).thenReturn(Optional.of(testPlace));

        Place result = placeService.getPlaceById(1L);

        assertNotNull(result);
        assertEquals("Jazz Bar", result.getName());
    }

    @Test
    void getPlaceById_WhenNotFound_ThrowsException() {
        when(placeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> placeService.getPlaceById(99L));
    }

    @Test
    void getPlaceById_WhenInactive_ThrowsException() {
        testPlace.setIsActive(false);
        when(placeRepository.findById(1L)).thenReturn(Optional.of(testPlace));

        assertThrows(IllegalArgumentException.class, () -> placeService.getPlaceById(1L));
    }

    @Test
    void createPlace_WhenValid_CreatesPlace() {
        when(userService.getUserById(1L)).thenReturn(creator);
        when(placeRepository.save(any(Place.class))).thenAnswer(inv -> inv.getArgument(0));

        Place result = placeService.createPlace(1L, "New Bar", "desc", PlaceType.BAR,
                "Street 1", 55.7, 37.6, null, null, null);

        assertNotNull(result);
        assertEquals("New Bar", result.getName());
        assertEquals(PlaceType.BAR, result.getType());
        verify(placeRepository).save(any(Place.class));
    }

    @Test
    void deletePlace_DeactivatesPlace() {
        when(placeRepository.findById(1L)).thenReturn(Optional.of(testPlace));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        placeService.deletePlace(1L);

        assertFalse(testPlace.getIsActive());
        verify(placeRepository).save(testPlace);
    }

    @Test
    void addTagsToPlace_AddsTagsAndIncreasesCount() {
        Tag tag = Tag.builder().id(10L).name("Jazz").usageCount(0).build();
        when(placeRepository.findById(1L)).thenReturn(Optional.of(testPlace));
        when(tagRepository.findAllById(Set.of(10L))).thenReturn(List.of(tag));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Place result = placeService.addTagsToPlace(1L, Set.of(10L));

        assertTrue(result.getTags().contains(tag));
        assertEquals(1, tag.getUsageCount());
    }

    @Test
    void getAllPlaces_ReturnsActiveList() {
        when(placeRepository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testPlace));

        List<Place> result = placeService.getAllPlaces();

        assertEquals(1, result.size());
    }

    @Test
    void findNearby_CallsRepository() {
        when(placeRepository.findNearbyPlaces(55.7, 37.6, 1000)).thenReturn(List.of(testPlace));

        List<Place> result = placeService.findNearby(55.7, 37.6, 1000);

        assertEquals(1, result.size());
        verify(placeRepository).findNearbyPlaces(55.7, 37.6, 1000);
    }
}
