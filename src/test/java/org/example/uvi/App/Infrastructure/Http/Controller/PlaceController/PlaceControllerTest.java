package org.example.uvi.App.Infrastructure.Http.Controller.PlaceController;

import tools.jackson.databind.ObjectMapper;
import org.example.uvi.App.Domain.Enums.PlaceType.PlaceType;
import org.example.uvi.App.Domain.Models.Place.Place;
import org.example.uvi.App.Domain.Services.PlaceService.PlaceService;
import org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto.CreatePlaceRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.PlaceDto.PlaceDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.PlaceMapper.PlaceMapper;
import org.example.uvi.App.Infrastructure.Security.JwtAuthFilter;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceService placeService;

    @MockitoBean
    private PlaceMapper placeMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getAllPlaces_ShouldReturnList() throws Exception {
        when(placeService.getAllPlaces()).thenReturn(List.of(new Place()));
        when(placeMapper.toDto(any())).thenReturn(new PlaceDto(1L, "Test Place", "Desc", PlaceType.PARK, "Address", 56.0, 60.0, null, null, null, null, null, null, true, 1L, null, null));

        mockMvc.perform(get("/api/v1/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Place"));
    }

    @Test
    void createPlace_ShouldReturnCreated() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        CreatePlaceRequest request = new CreatePlaceRequest("New Park", "Desc", PlaceType.PARK, "Address", 56.0, 60.0, null, null, null, null, null, null);
        
        when(placeService.createPlace(anyLong(), anyString(), anyString(), any(), anyString(), anyDouble(), anyDouble(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Place());
        when(placeMapper.toDto(any())).thenReturn(new PlaceDto(1L, "New Park", "Desc", PlaceType.PARK, "Address", 56.0, 60.0, null, null, null, null, null, null, true, 1L, null, null));

        mockMvc.perform(post("/api/v1/places")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Park"));
    }

    @Test
    void getPlace_ShouldReturnPlace() throws Exception {
        when(placeService.getPlaceById(1L)).thenReturn(new Place());
        when(placeMapper.toDto(any())).thenReturn(new PlaceDto(1L, "Test Place", "Desc", PlaceType.PARK, "Address", 56.0, 60.0, null, null, null, null, null, null, true, 1L, null, null));

        mockMvc.perform(get("/api/v1/places/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
