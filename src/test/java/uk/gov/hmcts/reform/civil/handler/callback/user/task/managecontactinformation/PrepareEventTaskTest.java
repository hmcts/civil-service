package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@ExtendWith(MockitoExtension.class)
public class PrepareEventTaskTest {

    @InjectMocks
    private PrepareEventTask handler;

    private ObjectMapper mapper;

    @Mock
    private UserService userService;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    private static final UserInfo ADMIN_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-admin"))
        .uid("admin-uid")
        .build();

    private static final UserInfo SOLICITOR_USER = UserInfo.builder()
        .roles(List.of("caseworker-civil-solicitor"))
        .uid("solicitor-uid")
        .build();

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        handler = new PrepareEventTask(userService, mapper, coreCaseUserService);
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldReturnErrorWhenNonAdminCaseBeforeAwaitingApplicantIntention() {
        // Case state is before Awaiting Applicant Intention for non-admin user
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
        when(userService.getUserInfo(anyString())).thenReturn(SOLICITOR_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.prepareEvent(caseData, "authToken");

        assertThat(response.getErrors()).contains("You will be able to run the manage contact information event once the claimant has responded.");
    }

    @Test
    void shouldNotReturnErrorForAdminCaseBeforeAwaitingApplicantIntention() {
        // Admin user with case before Awaiting Applicant Intention
        Party applicant1 = PartyBuilder.builder().company().build();
        Party respondent1 = PartyBuilder.builder().company().build();
        CaseData caseData = CaseDataBuilder.builder().applicant1(applicant1).respondent1(respondent1).build();
        caseData.setCcdState(AWAITING_APPLICANT_INTENTION);
        caseData.setCcdCaseReference(123456789L);
        when(userService.getUserInfo(anyString())).thenReturn(ADMIN_USER);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.prepareEvent(caseData, "authToken");

        assertThat(response.getErrors()).isNull();
    }

}
