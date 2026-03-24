package org.example.uvi.App.Infrastructure.Http.Controller.UserInterestController;

import org.example.uvi.App.Domain.Enums.Interest.Interest;
import org.example.uvi.App.Domain.Models.UserInterest.UserInterest;
import org.example.uvi.App.Domain.Services.UserInterestService.UserInterestService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserInterestDto.UserInterestDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserInterestMapper.UserInterestMapper;
import org.example.uvi.App.Infrastructure.Security.JwtAuthFilter;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserInterestController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserInterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserInterestService userInterestService;

    @MockitoBean
    private UserInterestMapper userInterestMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getMyInterests_ShouldReturnList() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        UserInterest interest = new UserInterest();
        UserInterestDto dto = new UserInterestDto(1L, Interest.SPORTS, "Sports", 5, null, null);

        when(userInterestService.getUserInterests(anyLong())).thenReturn(List.of(interest));
        when(userInterestMapper.toDto(interest)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/interests")
                        .principal(auth))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].interest").value("SPORTS"));
    }
}
