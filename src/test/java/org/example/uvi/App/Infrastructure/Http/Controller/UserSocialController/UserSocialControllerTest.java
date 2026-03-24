package org.example.uvi.App.Infrastructure.Http.Controller.UserSocialController;

import org.example.uvi.App.Domain.Models.UserFavorite.UserFavorite;
import org.example.uvi.App.Domain.Services.UserSocialService.UserSocialService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserSocialDto.UserFavoriteDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserSocialMapper.UserSocialMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserSocialController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserSocialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserSocialService socialService;

    @MockitoBean
    private UserSocialMapper socialMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void addToFavorites_ShouldReturnCreated() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());

        mockMvc.perform(post("/api/v1/user/social/favorites/42")
                        .with(csrf())
                        .principal(auth))
                .andExpect(status().isCreated());

        verify(socialService).addToFavorites(1L, 42L);
    }

    @Test
    void getFavorites_ShouldReturnList() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        UserFavorite favorite = new UserFavorite();
        UserFavoriteDto dto = new UserFavoriteDto(1L, 42L, "Place Name", "photo.jpg", null);

        when(socialService.getUserFavorites(anyLong())).thenReturn(List.of(favorite));
        when(socialMapper.toFavoriteDto(any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/social/favorites")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].placeName").value("Place Name"));
    }
}
