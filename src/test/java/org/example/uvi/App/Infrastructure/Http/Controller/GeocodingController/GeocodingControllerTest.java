package org.example.uvi.App.Infrastructure.Http.Controller.GeocodingController;

import org.example.uvi.App.Domain.Services.GeocodingService.GeocodingService;
import org.example.uvi.App.Infrastructure.Http.Dto.GeocodingDto.GeocodingResultDto;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeocodingController.class)
@AutoConfigureMockMvc(addFilters = false)
class GeocodingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeocodingService geocodingService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void search_ShouldReturnResults() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        GeocodingResultDto result = new GeocodingResultDto("Park", "Address", 56.0, 60.0, "PLACE");
        
        when(geocodingService.search(anyString())).thenReturn(List.of(result));

        mockMvc.perform(get("/api/v1/geocoding/search")
                        .param("query", "park")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Park"));
    }
}
