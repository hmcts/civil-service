package uk.gov.hmcts.reform.ucmc.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ucmc.model.ClaimValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.ucmc.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.ucmc.helpers.DateFormatHelper.formatLocalDateTime;

@WebMvcTest(CreateClaimController.class)
public class CreateClaimControllerTest {
    static final String USER_AUTH_TOKEN = "Bearer token";
    static final String USER_ID = "1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    void shouldReturnExpectedErrorInMidEventWhenValuesAreInvalid() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("claimValue", ClaimValue.builder().higherValue(1).lowerValue(10).build());

        CallbackRequest callbackRequest = toCallbackRequest(data);
        AboutToStartOrSubmitCallbackResponse callbackResponse  = postMidEvent(callbackRequest);

        assertThat(callbackResponse.getErrors())
            .containsOnly("CONTENT TBC: Higher value must not be lower than the lower value.");
    }

    @Test
    void shouldReturnNoErrorInMidEventWhenValuesAreValid() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("claimValue", ClaimValue.builder().higherValue(10).lowerValue(1).build());

        CallbackRequest callbackRequest = toCallbackRequest(data);
        AboutToStartOrSubmitCallbackResponse callbackResponse =  postMidEvent(callbackRequest);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorInMidEventWhenNoValues() throws Exception {
        CallbackRequest callbackRequest = toCallbackRequest(new HashMap<>());
        AboutToStartOrSubmitCallbackResponse callbackResponse =  postMidEvent(callbackRequest);

        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnExpectedSubmittedCallbackResponseObject() throws Exception {
        MvcResult response = mockMvc
            .perform(post("/create-claim/submitted")
                         .header("authorization", USER_AUTH_TOKEN)
                         .header("user-id", USER_ID)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(""))
            .andExpect(status().isOk())
            .andReturn();

        byte[] responseBody = response.getResponse().getContentAsByteArray();

        SubmittedCallbackResponse callbackResponse = toSubmittedCallbackResponse(responseBody);

        String documentLink = "https://www.google.com";
        String responsePackLink = "https://formfinder.hmctsformfinder.justice.gov.uk/n9-eng.pdf";
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        String body = format(
            "<br />Follow these steps to serve a claim:"
                + "\n* [Download the sealed claim form](%s) (PDF, 123KB)"
                + "\n* Send the form, particulars of claim and [a response pack](%s) (PDF, 266 KB) "
                + "to the defendant by %s"
                + "\n* Confirm service online within 21 days of sending the form, particulars and response pack, before"
                + " 4pm if you're doing this on the due day", documentLink, responsePackLink, formattedServiceDeadline);

        assertThat(callbackResponse).isEqualToComparingFieldByField(
            SubmittedCallbackResponse.builder()
                .confirmationHeader("# Your claim has been issued\n## Claim number: TBC")
                .confirmationBody(body)
                .build());
    }

    private CallbackRequest toCallbackRequest(Map<String, Object> data) {
        return CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(data).build())
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse postMidEvent(CallbackRequest callbackRequest) throws Exception {
        MvcResult response = mockMvc
            .perform(post("/create-claim/mid-event")
                         .header("authorization", USER_AUTH_TOKEN)
                         .header("user-id", USER_ID)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(toBytes(callbackRequest)))
            .andExpect(status().isOk())
            .andReturn();

        byte[] responseBody = response.getResponse().getContentAsByteArray();

        return toAboutToSubmitResponse(responseBody);
    }

    private AboutToStartOrSubmitCallbackResponse toAboutToSubmitResponse(byte[] body) throws java.io.IOException {
        if (body.length > 0) {
            return mapper.readValue(body, AboutToStartOrSubmitCallbackResponse.class);
        } else {
            return null;
        }
    }

    private byte[] toBytes(Object o) {
        try {
            return mapper.writeValueAsString(o).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private SubmittedCallbackResponse toSubmittedCallbackResponse(byte[] responseBody) throws java.io.IOException {
        if (responseBody.length > 0) {
            return mapper.readValue(responseBody, SubmittedCallbackResponse.class);
        } else {
            return null;
        }
    }
}
