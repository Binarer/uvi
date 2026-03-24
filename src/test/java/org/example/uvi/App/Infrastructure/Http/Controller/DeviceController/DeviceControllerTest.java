package org.example.uvi.App.Infrastructure.Http.Controller.DeviceController;

import org.example.uvi.App.Domain.Models.Device.Device;
import org.example.uvi.App.Domain.Services.DeviceService.DeviceService;
import org.example.uvi.App.Infrastructure.Http.Dto.DeviceDto.DeviceDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.DeviceMapper.DeviceMapper;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private DeviceMapper deviceMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getMyDevices_ShouldReturnList() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        Device device = new Device();
        DeviceDto dto = new DeviceDto(UUID.randomUUID(), 1L, "token", null, null);

        when(deviceService.getDevicesByUser(anyLong())).thenReturn(List.of(device));
        when(deviceMapper.toDto(device)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/devices")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceToken").value("token"));
    }
}
