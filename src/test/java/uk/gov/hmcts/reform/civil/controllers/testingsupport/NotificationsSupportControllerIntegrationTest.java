package uk.gov.hmcts.reform.civil.controllers.testingsupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditEntry;
import uk.gov.hmcts.reform.civil.notify.audit.NotificationAuditService;

import java.time.Instant;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationsSupportControllerIntegrationTest {

    private static final String CASE_ID = "001MC123";
    private static final String ENDPOINT = "/testing-support/notifications/sent";

    @Mock
    private NotificationAuditService notificationAuditService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(WRITE_DATES_AS_TIMESTAMPS);
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders
            .standaloneSetup(new NotificationsSupportController(notificationAuditService))
            .setMessageConverters(converter)
            .build();
    }

    @Test
    void shouldReturnSentNotificationsAsJson() throws Exception {
        NotificationAuditEntry entry = new NotificationAuditEntry(
            "template-1",
            "claimant@email.com",
            "received-001MC123",
            "notif-1",
            Instant.parse("2026-04-30T10:00:00Z")
        );
        given(notificationAuditService.query(CASE_ID)).willReturn(List.of(entry));

        mockMvc.perform(get(ENDPOINT).param("caseId", CASE_ID))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].templateId").value("template-1"))
            .andExpect(jsonPath("$[0].recipientEmail").value("claimant@email.com"))
            .andExpect(jsonPath("$[0].reference").value("received-001MC123"))
            .andExpect(jsonPath("$[0].notificationId").value("notif-1"))
            .andExpect(jsonPath("$[0].sentAt").value("2026-04-30T10:00:00Z"));
    }

    @Test
    void shouldFilterByTemplateIdAndRecipientEmailQueryParams() throws Exception {
        NotificationAuditEntry first = new NotificationAuditEntry(
            "template-1", "claimant@email.com", "ref-1", "notif-1", Instant.now()
        );
        NotificationAuditEntry second = new NotificationAuditEntry(
            "template-1", "defendant@email.com", "ref-2", "notif-2", Instant.now()
        );
        given(notificationAuditService.query(CASE_ID)).willReturn(List.of(first, second));

        mockMvc.perform(get(ENDPOINT)
                .param("caseId", CASE_ID)
                .param("templateId", "template-1")
                .param("recipientEmail", "defendant@email.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].notificationId").value("notif-2"));
    }

    @Test
    void shouldReturnEmptyJsonArrayWhenNoEntriesMatch() throws Exception {
        given(notificationAuditService.query(CASE_ID)).willReturn(List.of());

        mockMvc.perform(get(ENDPOINT).param("caseId", CASE_ID))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnBadRequestWhenCaseIdParamMissing() throws Exception {
        mockMvc.perform(get(ENDPOINT))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCaseIdParamIsEmptyString() throws Exception {
        mockMvc.perform(get(ENDPOINT).param("caseId", ""))
            .andExpect(status().isBadRequest());
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldReturnBadRequestWhenCaseIdParamIsWhitespace() throws Exception {
        mockMvc.perform(get(ENDPOINT).param("caseId", "   "))
            .andExpect(status().isBadRequest());
        verify(notificationAuditService, never()).query(any());
    }

    @Test
    void shouldReturnMethodNotAllowedForPostRequest() throws Exception {
        mockMvc.perform(post(ENDPOINT).param("caseId", CASE_ID))
            .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldHandleUrlEncodedSpecialCharactersInCaseId() throws Exception {
        String caseIdWithSpace = "001 MC 123";
        given(notificationAuditService.query(caseIdWithSpace)).willReturn(List.of());

        mockMvc.perform(get(ENDPOINT).param("caseId", caseIdWithSpace))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
        verify(notificationAuditService).query(caseIdWithSpace);
    }

    @Test
    void shouldIgnoreBlankTemplateIdQueryParam() throws Exception {
        NotificationAuditEntry entry = new NotificationAuditEntry(
            "template-1", "a@email.com", "ref-1", "notif-1", Instant.now()
        );
        given(notificationAuditService.query(CASE_ID)).willReturn(List.of(entry));

        mockMvc.perform(get(ENDPOINT)
                .param("caseId", CASE_ID)
                .param("templateId", ""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}
