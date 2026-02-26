package uk.gov.hmcts.reform.civil.ga.service.events;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GaCaseEventServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private GaCoreCaseDataService gaCoreCaseDataService;

    @InjectMocks
    private GaCaseEventService gaCaseEventService;

    private static final String EVENT_TOKEN = "jM4OWUxMGRkLWEyMzYt";
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .id(1L)
        .data(Map.of())
        .build();
    private static final String AUTHORISATION = "authorisation";
    private static final String USER_ID = "123";
    private static final String CASE_ID = "123";
    private static final String EVENT_ID = "1";
    private static final StartEventResponse RESPONSE = StartEventResponse
        .builder()
        .eventId(EVENT_ID)
        .token(EVENT_TOKEN)
        .caseDetails(CASE_DETAILS)
        .build();

    @BeforeEach
    void setUp() {
        given(authTokenGenerator.generate()).willReturn(EVENT_TOKEN);
        given(coreCaseDataApi.startEventForCitizen(any(), any(), any(), any(), any(), any(), any()))
            .willReturn(RESPONSE);
        given(coreCaseDataApi.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
    }

    @Test
    void shouldSubmitEventForExistingClaimSuccessfully() {
        when(gaCoreCaseDataService.caseDataContentFromStartEventResponse(any(), anyMap())).thenCallRealMethod();

        CaseDetails caseDetails = gaCaseEventService.submitEvent(new EventSubmissionParams()
                                                                   .setUpdates(Maps.newHashMap())
                                                                   .setEvent(CaseEvent.RESPOND_TO_APPLICATION)
                                                                   .setCaseId(CASE_ID)
                                                                   .setUserId(USER_ID)
                                                                   .setAuthorisation(AUTHORISATION));
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);

        StartEventResponse startEventResponse = gaCaseEventService
            .startEvent(AUTHORISATION, USER_ID, CASE_ID, CaseEvent.RESPOND_TO_APPLICATION);
        assertThat(startEventResponse).isEqualTo(RESPONSE);
    }
}
