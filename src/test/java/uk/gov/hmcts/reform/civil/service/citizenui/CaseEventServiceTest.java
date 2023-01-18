package uk.gov.hmcts.reform.civil.service.citizenui;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@ExtendWith(SpringExtension.class)
public class CaseEventServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CaseEventService caseEventService;

    private static final String EVENT_TOKEN = "jM4OWUxMGRkLWEyMzYt";
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .id(1L)
        .data(Map.of())
        .build();
    private static final String AUTHORISATION = "authorisation";
    private static final String USER_ID = "123";
    private static final String CASE_ID = "123";
    private static final String DRAFT = "draft";
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
        given(coreCaseDataApi.startForCitizen(any(), any(), any(), any(), any(), any()))
            .willReturn(RESPONSE);
        given(coreCaseDataApi.submitEventForCitizen(any(), any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
        given(coreCaseDataApi.submitForCitizen(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
    }

    @Test
    void shouldSubmitEventForExistingClaimSuccessfully() {
        InOrder orderVerifier = inOrder(coreCaseDataApi);
        CaseDetails caseDetails = caseEventService.submitEvent(EventSubmissionParams
                                                                   .builder()
                                                                   .updates(Maps.newHashMap())
                                                                   .event(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .caseId(CASE_ID)
                                                                   .userId(USER_ID)
                                                                   .authorisation(AUTHORISATION)
                                                                   .build());
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startEventForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            CaseEvent.DEFENDANT_RESPONSE_SPEC.name()
        );
        orderVerifier.verify(coreCaseDataApi).submitEventForCitizen(AUTHORISATION, EVENT_TOKEN, USER_ID, JURISDICTION,
                                                                    CASE_TYPE, CASE_ID, true,
                                                                    caseDataContentFromStartEventResponse(
                                                                        RESPONSE,
                                                                        Map.of()
                                                                    )
        );
    }

    @Test
    void shouldSubmitEventForNewClaimSuccessfully() {
        InOrder orderVerifier = inOrder(coreCaseDataApi);
        CaseDetails caseDetails = caseEventService.submitEvent(EventSubmissionParams
                                                                   .builder()
                                                                   .updates(Maps.newHashMap())
                                                                   .event(CaseEvent.DEFENDANT_RESPONSE_SPEC)
                                                                   .caseId(DRAFT)
                                                                   .userId(USER_ID)
                                                                   .authorisation(AUTHORISATION)
                                                                   .build());
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        orderVerifier.verify(coreCaseDataApi).startForCitizen(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CaseEvent.DEFENDANT_RESPONSE_SPEC.name()
        );

        orderVerifier.verify(coreCaseDataApi).submitForCitizen(AUTHORISATION, EVENT_TOKEN, USER_ID, JURISDICTION,
                                                               CASE_TYPE, true,
                                                               caseDataContentFromStartEventResponse(
                                                                   RESPONSE,
                                                                   Map.of()
                                                               )
        );
    }
}
