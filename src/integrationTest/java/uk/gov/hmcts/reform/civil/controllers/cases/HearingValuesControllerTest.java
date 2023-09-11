package uk.gov.hmcts.reform.civil.controllers.cases;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.controllers.BaseIntegrationTest;
import uk.gov.hmcts.reform.civil.exceptions.MissingFieldsUpdatedException;
import uk.gov.hmcts.reform.civil.model.HearingValuesRequest;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.hearings.HearingValuesService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HearingValuesControllerTest extends BaseIntegrationTest {

    private static final String HEARING_VALUES_URL = "/serviceHearingValues";

    @MockBean
    private HearingValuesService hearingValuesService;

    @Test
    @SneakyThrows
    public void shouldReturnHttp200() {
        ServiceHearingValuesModel expectedHearingValues =
            ServiceHearingValuesModel.builder().publicCaseName("mock case name").build();

        when(hearingValuesService.getValues(anyLong(), anyString(), anyString()))
            .thenReturn(expectedHearingValues);

        HearingValuesRequest requestBody = HearingValuesRequest.builder().caseReference(1L).hearingId("hearingid").build();

        doPost(BEARER_TOKEN, requestBody, HEARING_VALUES_URL)
            .andExpect(content().json(toJson(expectedHearingValues)))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    public void shouldReturnHttp400_whenCaseNotFoundExceptionThrown() {
        when(hearingValuesService.getValues(anyLong(), anyString(), anyString()))
            .thenThrow(CaseNotFoundException.class);

        HearingValuesRequest requestBody = HearingValuesRequest.builder().caseReference(1L).hearingId("hearingid").build();

        doPost(BEARER_TOKEN, requestBody, HEARING_VALUES_URL)
            .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    public void shouldReturnHttp404_whenPartyIdsUpdatedExceptionThrown() {
        when(hearingValuesService.getValues(anyLong(), anyString(), anyString()))
            .thenThrow(MissingFieldsUpdatedException.class);

        HearingValuesRequest requestBody = HearingValuesRequest.builder().caseReference(1L).hearingId("hearingid").build();

        doPost(BEARER_TOKEN, requestBody, HEARING_VALUES_URL)
            .andExpect(status().isNotFound());
    }
}
