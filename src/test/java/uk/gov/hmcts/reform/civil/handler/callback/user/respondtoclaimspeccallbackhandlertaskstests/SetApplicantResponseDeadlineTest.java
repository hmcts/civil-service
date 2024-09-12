package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
public class SetApplicantResponseDeadlineTest {

    private SetApplicantResponseDeadline setApplicantResponseDeadline;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private StateFlow mockedStateFlow;

    @Mock
    private UserService userService;

    @Mock
    private Time time;

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    @Mock
    private FeatureToggleService toggleService;

    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @Mock
    private FrcDocumentsUtils frcDocumentsUtils;

    @Mock
    private DeadlineExtensionCalculatorService deadlineCalculatorService;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private AssignCategoryId assignCategoryId;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        setApplicantResponseDeadline = new SetApplicantResponseDeadline(
             userService,
             coreCaseUserService,
             toggleService,
             objectMapper,
             caseFlagsInitialiser,
             time,
             deadlineCalculatorService,
             stateFlowEngine,
             deadlinesCalculator,
             frcDocumentsUtils,
             respondToClaimSpecUtilsDisputeDetails,
             courtLocationUtils,
             respondToClaimSpecUtils,
             assignCategoryId
        );
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
    }

    @Test
    void shouldSetApplicantResponseDeadlineWhenMultiPartyScenarioOneVOne() {
        Address address = Address.builder()
            .addressLine1("123 Test Street")
            .postCode("AB12 3CD")
            .build();

        Party respondent1Copy = Party.builder()
            .primaryAddress(address)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent1Copy(respondent1Copy)
            .respondent1DQ(new Respondent1DQ())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSetApplicantResponseDeadlineWhenSolicitorRepresentsOnlyRespondentOne() {
        when(coreCaseUserService.userHasCaseRole(eq("1594901956117591"), eq("uid"), eq(RESPONDENTSOLICITORTWO)))
            .thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Address address = Address.builder()
            .addressLine1("123 Test Street")
            .postCode("AB12 3CD")
            .build();

        Party respondent1Copy = Party.builder()
            .primaryAddress(address)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent1Copy(respondent1Copy)
            .respondent2DQ(new Respondent2DQ())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldSetApplicantResponseDeadlineWhenRespondent2HasDifferentResponseAndExperts() {
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(true);
        when(time.now()).thenReturn(LocalDateTime.now());

        Address address = Address.builder()
            .addressLine1("123 Test Street")
            .postCode("AB12 3CD")
            .build();

        Party respondent1Copy = Party.builder()
            .primaryAddress(address)
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondentResponseIsSame(NO)
            .respondent1Copy(respondent1Copy)
            .respondent2DQ(new Respondent2DQ())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "Bearer token"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);

        assertNull(response.getErrors());
        assertThat(response).isNotNull();
    }
}
