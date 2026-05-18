package uk.gov.hmcts.reform.civil.handler.callback.testing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.model.CallbackErrorResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class ResourceExceptionHandlerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaseEventService caseEventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldHandleFeignUnprocessableEntityException() throws Exception {

        // given
        CallbackErrorResponse callbackErrorResponse = new CallbackErrorResponse();
        callbackErrorResponse.setCallbackErrors(List.of("Validation failed"));

        String errorJson = objectMapper.writeValueAsString(callbackErrorResponse);

        setUnprocessableEntity(errorJson);

        EventDto eventDto = new EventDto();
        eventDto.setEvent(CaseEvent.ASSIGN_LIP_DEFENDANT);
        eventDto.setCaseDataUpdate(Map.of("field", "value"));

        // when + then
        mockMvc.perform(post("/cases/123/citizen/user1/event")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.callbackErrors[0]")
                           .value("Validation failed"));
    }

    @Test
    void shouldReturnFallbackResponseWhenErrorBodyCannotBeParsed() throws Exception {

        // given
        String invalidJson = "not-json";

        setUnprocessableEntity(invalidJson);

        EventDto eventDto = new EventDto();
        eventDto.setEvent(CaseEvent.ASSIGN_LIP_DEFENDANT);
        eventDto.setCaseDataUpdate(Map.of());

        // when + then
        mockMvc.perform(post("/cases/123/citizen/user1/event")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(eventDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.callbackErrors[0]")
                           .value("Unable to parse error response"));
    }

    private void setUnprocessableEntity(final String errorJson) {
        Request request = Request.create(
            Request.HttpMethod.POST,
            "/cases/123/citizen/user1/event",
            Map.of(),
            null,
            StandardCharsets.UTF_8,
            null
        );

        FeignException.UnprocessableEntity exception =
            new FeignException.UnprocessableEntity(
                "422 Unprocessable Entity",
                request,
                errorJson.getBytes(StandardCharsets.UTF_8),
                Map.of()
            );

        when(caseEventService.submitEvent(any(EventSubmissionParams.class)))
            .thenThrow(exception);
    }
}
