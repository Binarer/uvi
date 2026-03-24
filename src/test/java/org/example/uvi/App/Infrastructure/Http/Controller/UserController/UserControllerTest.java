package org.example.uvi.App.Infrastructure.Http.Controller.UserController;

import tools.jackson.databind.ObjectMapper;
import org.example.uvi.App.Domain.Models.User.User;
import org.example.uvi.App.Domain.Services.UserService.UserService;
import org.example.uvi.App.Infrastructure.Http.Dto.UserDto.UserDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.UserMapper.UserMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getCurrentUser_ShouldReturnProfile() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        
        when(userService.getUserById(anyLong())).thenReturn(new User());
        when(userMapper.toDto(any())).thenReturn(new UserDto(1L, "Ivan", "Ivanov", "79123456789", "ivan_ekb", null, null, null, "Ekb", 56.0, 60.0, true, null, null));

        mockMvc.perform(get("/api/v1/users/me")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ivan_ekb"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        
        when(userService.getUserById(1L)).thenReturn(new User());
        when(userMapper.toDto(any())).thenReturn(new UserDto(1L, "Ivan", "Ivanov", "79123456789", "ivan_ekb", null, null, null, "Ekb", 56.0, 60.0, true, null, null));

        mockMvc.perform(get("/api/v1/users/1")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
