package uk.gov.hmcts.reform.civil.service.citizen.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_LIP_DEFENDANT;

@ExtendWith(SpringExtension.class)
class LipDefendantCaseAssignmentServiceTest {

    private static final String AUTHORIZATION = "authorization";
    private static final String CASE_ID = "caseId";
    private static final String USER_ID = "userId";
    private static final String EMAIL = "email@email.com";

    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private IdamClient idamClient;
    @Mock
    private CaseEventService caseEventService;
    @InjectMocks
    private LipDefendantCaseAssignmentService lipDefendantCaseAssignmentService;

    @Test
    void shouldAddDefendantDetails_whenLipVLipFlagIsEnabled() {
        //Given
        given(featureToggleService.isLipVLipEnabled()).willReturn(true);
        given(idamClient.getUserDetails(anyString())).willReturn(UserDetails.builder().id(USER_ID).email(EMAIL).build());
        IdamUserDetails defendantUserDetails = IdamUserDetails.builder()
            .id(USER_ID)
            .email(EMAIL)
            .build();
        Map<String, Object> data = Map.of("defendantUserDetails", defendantUserDetails);
        EventSubmissionParams params = EventSubmissionParams.builder()
            .caseId(CASE_ID)
            .userId(USER_ID)
            .authorisation(AUTHORIZATION)
            .event(ASSIGN_LIP_DEFENDANT)
            .updates(data)
            .build();
        //When
        lipDefendantCaseAssignmentService.addLipDefendantToCaseDefendantUserDetails(AUTHORIZATION, CASE_ID);
        //Then
        verify(idamClient).getUserDetails(AUTHORIZATION);
        verify(caseEventService).submitEventForClaim(refEq(params));
    }

}
