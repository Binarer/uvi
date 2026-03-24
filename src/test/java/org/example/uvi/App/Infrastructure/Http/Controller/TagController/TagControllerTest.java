package org.example.uvi.App.Infrastructure.Http.Controller.TagController;

import org.example.uvi.App.Domain.Models.Tag.Tag;
import org.example.uvi.App.Domain.Services.TagService.TagService;
import org.example.uvi.App.Infrastructure.Http.Dto.TagDto.TagDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.TagMapper.TagMapper;
import org.example.uvi.App.Infrastructure.Security.JwtAuthFilter;
import org.example.uvi.App.Infrastructure.Security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
@AutoConfigureMockMvc(addFilters = false)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private TagMapper tagMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getAllTags_ShouldReturnList() throws Exception {
        Tag tag = new Tag();
        TagDto dto = new TagDto(1L, "Family Friendly", null, "Desc", 0, null);

        when(tagService.getAllTags()).thenReturn(List.of(tag));
        when(tagMapper.toDto(tag)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Family Friendly"));
    }
}
