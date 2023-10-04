package uk.gov.hmcts.reform.civil.service.bulkclaims;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter.caseDataContentFromStartEventResponse;

@ExtendWith(SpringExtension.class)
public class CaseworkerCaseEventServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CaseworkerCaseEventService caseworkerCaseEventService;

    private static final String EVENT_TOKEN = "jM4OWUxMGRkLWEyMzYt";
    private static final CaseDetails CASE_DETAILS = CaseDetails.builder()
        .data(Map.of())
        .build();
    private static final String AUTHORISATION = "authorisation";
    private static final String USER_ID = "userId";
    private static final String EVENT_ID = "CREATE_CLAIM_SPEC";
    private static final StartEventResponse START_EVENT_RESPONSE = StartEventResponse
        .builder()
        .eventId(EVENT_ID)
        .token(EVENT_TOKEN)
        .caseDetails(CASE_DETAILS)
        .build();

    @BeforeEach
    void setUp() {
        given(authTokenGenerator.generate()).willReturn(EVENT_TOKEN);
        given(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any()))
            .willReturn(START_EVENT_RESPONSE);
        given(coreCaseDataApi.submitForCaseworker(any(), any(), any(), any(), any(), anyBoolean(), any()))
            .willReturn(CASE_DETAILS);
    }

    @Test
    void shouldSubmitCaseworkerEventForNewClaimSuccessfully() {
        CaseDetails caseDetails = caseworkerCaseEventService.submitEventForNewClaimCaseWorker(CaseworkerEventSubmissionParams
                                                                   .builder()
                                                                   .updates(Maps.newHashMap())
                                                                   .event(CaseEvent.CREATE_CLAIM_SPEC)
                                                                   .userId(USER_ID)
                                                                   .authorisation(AUTHORISATION)
                                                                   .build());
        assertThat(caseDetails).isEqualTo(CASE_DETAILS);
        verify(coreCaseDataApi).startForCaseworker(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            CaseEvent.CREATE_CLAIM_SPEC.name()
        );

        verify(coreCaseDataApi).submitForCaseworker(
            AUTHORISATION,
            EVENT_TOKEN,
            USER_ID,
            JURISDICTION,
            CASE_TYPE,
            true,
            caseDataContentFromStartEventResponse(START_EVENT_RESPONSE, Map.of())
        );
    }
}
