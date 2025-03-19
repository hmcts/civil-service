package uk.gov.hmcts.reform.civil.service.citizen.defendant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private UserService userService;
    @Mock
    private CaseEventService caseEventService;
    @Mock
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    LipDefendantCaseAssignmentService lipDefendantCaseAssignmentService;

    @BeforeEach
    public void setUp() {
        lipDefendantCaseAssignmentService =
            new LipDefendantCaseAssignmentService(userService, caseEventService, defendantPinToPostLRspecService, caseDetailsConverter, false);
    }

    @Test
    void shouldAddDefendantDetails_whenLipVLipFlagIsEnabled() {
        //Given
        given(userService.getUserDetails(anyString())).willReturn(UserDetails.builder().id(USER_ID).email(EMAIL).build());
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Party respondent1 = caseData.getRespondent1()
            .toBuilder().partyEmail(EMAIL).build();
        IdamUserDetails defendantUserDetails = IdamUserDetails.builder()
            .id(USER_ID)
            .email(EMAIL)
            .build();
        Map<String, Object> data = Map.of("defendantUserDetails", defendantUserDetails,
                                          "respondent1", respondent1);

        when(caseDetailsConverter.toCaseData((CaseDetails) any())).thenReturn(caseData);
        EventSubmissionParams params = EventSubmissionParams.builder()
            .caseId(CASE_ID)
            .userId(USER_ID)
            .authorisation(AUTHORIZATION)
            .event(ASSIGN_LIP_DEFENDANT)
            .updates(data)
            .build();
        //When
        lipDefendantCaseAssignmentService.addLipDefendantToCaseDefendantUserDetails(
            AUTHORIZATION,
            CASE_ID,
            Optional.empty(),
            Optional.of(CaseDetailsBuilder.builder()
                            .data(caseData)
                            .build())
        );
        //Then
        verify(userService).getUserDetails(AUTHORIZATION);
        verify(caseEventService).submitEventForClaim(refEq(params));
    }

    @Test
    void shouldRemovePinPostDetails_whenLipVLipFlagIsEnabled() {
        //Given
        DefendantPinToPostLRspec pinInPostData = DefendantPinToPostLRspec.builder()
            .expiryDate(LocalDate.now().plusDays(180))
            .build();
        Map<String, Object> data = new HashMap<>();
        data.put("respondent1PinToPostLRspec", pinInPostData);
        given(userService.getUserDetails(anyString())).willReturn(UserDetails.builder().id(USER_ID).email(EMAIL)
                                                                     .build());
        given(defendantPinToPostLRspecService.removePinInPostData(any())).willReturn(data);

        CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
            .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                               .accessCode("TEST1234")
                                               .expiryDate(LocalDate.now().plusDays(180))
                                               .build())
            .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
            .respondent1(Party.builder()
                             .flags(Flags.builder()
                                        .partyName("Mr test")
                                        .roleOnCase("Defendant 1")
                                        .details(List.of()).build())
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .build();
        IdamUserDetails defendantUserDetails = IdamUserDetails.builder()
            .id(USER_ID)
            .email(EMAIL)
            .build();
        data.put("defendantUserDetails", defendantUserDetails);
        data.put("respondent1", caseData.getRespondent1().toBuilder().partyEmail(EMAIL).build());
        ReflectionTestUtils.setField(lipDefendantCaseAssignmentService, "caseFlagsLoggingEnabled", true);
        Optional<CaseDetails> caseDetails = Optional.of(CaseDetailsBuilder.builder().data(caseData).build());
        when(caseDetailsConverter.toCaseData(caseDetails.get())).thenReturn(caseData);
        EventSubmissionParams params = EventSubmissionParams.builder()
            .caseId(CASE_ID)
            .userId(USER_ID)
            .authorisation(AUTHORIZATION)
            .event(ASSIGN_LIP_DEFENDANT)
            .updates(data)
            .build();
        //When
        lipDefendantCaseAssignmentService.addLipDefendantToCaseDefendantUserDetails(
            AUTHORIZATION,
            CASE_ID,
            Optional.of(CaseRole.DEFENDANT),
            caseDetails
        );
        //Then
        verify(userService).getUserDetails(AUTHORIZATION);
        verify(caseEventService).submitEventForClaim(refEq(params));
    }
}
