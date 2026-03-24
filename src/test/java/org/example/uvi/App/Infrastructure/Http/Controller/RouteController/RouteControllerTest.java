package org.example.uvi.App.Infrastructure.Http.Controller.RouteController;

import tools.jackson.databind.ObjectMapper;
import org.example.uvi.App.Domain.Enums.RouteMode.RouteMode;
import org.example.uvi.App.Domain.Services.RouteService.RouteService;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto.RouteDto;
import org.example.uvi.App.Infrastructure.Http.Dto.RouteDto.RouteRequest;
import org.example.uvi.App.Infrastructure.Security.JwtAuthFilter;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RouteService routeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void calculateRoute_ShouldReturnRoute() throws Exception {
        RouteRequest request = new RouteRequest(56.0, 60.0, 56.1, 60.1, RouteMode.DRIVING);
        RouteDto response = new RouteDto(56.0, 60.0, 56.1, 60.1, RouteMode.DRIVING, 1000.0, 1.0, 10, List.of());

        when(routeService.calculateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/routes/calculate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDistanceMeters").value(1000.0));
    }
}
