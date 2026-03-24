package org.example.uvi.App.Infrastructure.Http.Controller.InvitationController;

import org.example.uvi.App.Domain.Models.Family.FamilyInvitation;
import org.example.uvi.App.Domain.Services.FamilyInvitationService.FamilyInvitationService;
import org.example.uvi.App.Infrastructure.Http.Dto.InvitationDto.InvitationDto;
import org.example.uvi.App.Infrastructure.Http.Mapper.InvitationMapper.InvitationMapper;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InvitationController.class)
@AutoConfigureMockMvc(addFilters = false)
class InvitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FamilyInvitationService invitationService;

    @MockitoBean
    private InvitationMapper invitationMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getMyPendingInvitations_ShouldReturnList() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());
        FamilyInvitation invitation = new FamilyInvitation();
        InvitationDto dto = new InvitationDto(1L, 10L, "Family", 2L, "Inviter", 1L, "Invitee", "79123456789", null, "CODE", null, null, null, null);

        when(invitationService.getUserPendingInvitations(anyLong())).thenReturn(List.of(invitation));
        when(invitationMapper.toDto(invitation)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/invitations/my/pending")
                        .principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invitationCode").value("CODE"));
    }

    @Test
    void acceptInvitation_ShouldReturnOk() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList());

        mockMvc.perform(post("/api/v1/invitations/CODE/accept")
                        .with(csrf())
                        .principal(auth))
                .andExpect(status().isNoContent());

        verify(invitationService).acceptInvitation("CODE", 1L);
    }
}
