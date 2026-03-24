package org.example.uvi.App.Infrastructure.Http.Controller.FamilyController;

import tools.jackson.databind.ObjectMapper;
import org.example.uvi.App.Domain.Models.Family.Family;
import org.example.uvi.App.Domain.Services.FamilyService.FamilyService;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.CreateFamilyRequest;
import org.example.uvi.App.Infrastructure.Http.Dto.FamilyDto.FamilyDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.FamilyMapper.FamilyMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FamilyService familyService;

    @MockitoBean
    private FamilyMapper familyMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void createFamily_ShouldReturnCreated() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        CreateFamilyRequest request = new CreateFamilyRequest("The Browns", "Description", "http://avatar.url");
        
        when(familyService.createFamily(anyLong(), anyString(), anyString(), anyString())).thenReturn(new Family());
        when(familyMapper.toDto(any())).thenReturn(new FamilyDto(1L, "The Browns", "Desc", null, null, 1L, "Admin", 10, 1, List.of(), null));

        mockMvc.perform(post("/api/v1/families")
                        .with(csrf())
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("The Browns"));
    }

    @Test
    void getAllFamilies_ShouldReturnList() throws Exception {
        when(familyService.getAllFamilies()).thenReturn(List.of(new Family()));
        when(familyMapper.toDto(any())).thenReturn(new FamilyDto(1L, "The Browns", "Desc", null, null, 1L, "Admin", 10, 1, List.of(), null));

        mockMvc.perform(get("/api/v1/families"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("The Browns"));
    }

    @Test
    void leaveFamily_ShouldReturnNoContent() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());

        mockMvc.perform(post("/api/v1/families/leave")
                        .with(csrf())
                        .principal(auth))
                .andExpect(status().isNoContent());

        verify(familyService).leaveFamily(1L);
    }

    @Test
    void deleteFamily_ShouldReturnNoContent() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());

        mockMvc.perform(delete("/api/v1/families/1")
                        .with(csrf())
                        .principal(auth))
                .andExpect(status().isNoContent());

        verify(familyService).deleteFamily(1L, 1L);
    }
}
