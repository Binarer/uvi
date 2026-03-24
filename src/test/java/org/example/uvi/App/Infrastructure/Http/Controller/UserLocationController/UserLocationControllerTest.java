package org.example.uvi.App.Infrastructure.Http.Controller.UserLocationController;

import org.example.uvi.App.Domain.Models.UserLocation.UserLocation;
import org.example.uvi.App.Domain.Services.UserLocationService.UserLocationService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserLocationDto.UserLocationDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserLocationMapper.UserLocationMapper;
import org.example.uvi.App.Infrastructure.Security.JwtAuthFilter;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserLocationController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserLocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserLocationService userLocationService;

    @MockitoBean
    private UserLocationMapper userLocationMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getMyLatestLocation_ShouldReturnLocation() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        UserLocation location = new UserLocation();
        UserLocationDto dto = new UserLocationDto(UUID.randomUUID(), 1L, 56.0, 60.0, 5.0f, 80, 1.0f, null);

        when(userLocationService.getLatestLocation(anyLong())).thenReturn(Optional.of(location));
        when(userLocationMapper.toDto(location)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/locations/me/latest")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(56.0));
    }
}
