package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertaskstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.AssembleDocumentsForDeadlineResponse;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.SetApplicantResponseDeadline;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.UpdateDataRespondentDeadlineResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.*;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SetApplicantResponseDeadlineTest {

    @InjectMocks
    private SetApplicantResponseDeadline setApplicantResponseDeadline;

    @Mock
    private UpdateDataRespondentDeadlineResponse updateDataRespondentDeadlineResponse;

    @Mock
    private  Time time;

    @Mock
    private  DeadlinesCalculator deadlinesCalculator;

    @Mock
    private  FrcDocumentsUtils frcDocumentsUtils;

    @Mock
    private  FeatureToggleService toggleService;

    @Mock
    private  CaseFlagsInitialiser caseFlagsInitialiser;

    @Mock
    private  IStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow mockedStateFlow;

    @Mock
    private  AssignCategoryId assignCategoryId;

    @Mock
    private  ObjectMapper objectMapper;

    @Mock
    private  CoreCaseUserService coreCaseUserService;

    @Mock
    private  UserService userService;

    @Mock
    private  LocationReferenceDataService locationRefDataService;

    @Mock
    private AssembleDocumentsForDeadlineResponse assembleDocumentsForDeadlineResponse;

    @Mock
    private  CourtLocationUtils courtLocationUtils;

    @Mock
    private RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;

    @BeforeEach
    void setUp() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
    }

    @Test
    void shouldHandleBothRespondentsWithSameLegalRep() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = responseDate.plusDays(4);

        when(deadlinesCalculator.calculateApplicantResponseDeadline(any(LocalDateTime.class))).thenReturn(deadline);
        when(time.now()).thenReturn(responseDate);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of("state", "created")).build())
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondentResponseIsSame(YES)
            .respondent2SameLegalRepresentative(YES)
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BearerToken"))
            .request(callbackRequest)
            .build();

        callbackParams.getRequest().getCaseDetailsBefore().setState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);

        assertEquals("AWAITING_APPLICANT_INTENTION", response.getState());

        verify(frcDocumentsUtils).assembleDefendantsFRCDocuments(caseData);
    }

    @Test
    void setApplicantResponseDeadlineWhenSolicitorRepresentsOnlyOneOfRespondents() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = responseDate.plusDays(4);

        when(deadlinesCalculator.calculateApplicantResponseDeadline(any(LocalDateTime.class))).thenReturn(deadline);
        when(time.now()).thenReturn(responseDate);
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());

        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of("state", "created")).build())
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondentResponseIsSame(NO)
            .respondent2SameLegalRepresentative(NO)
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BearerToken"))
            .request(callbackRequest)
            .build();

        callbackParams.getRequest().getCaseDetailsBefore().setState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);

        assertEquals("AWAITING_APPLICANT_INTENTION", response.getState());

        verify(frcDocumentsUtils).assembleDefendantsFRCDocuments(caseData);
    }

    @Test
    void shouldHandleDefaultResponse() {
        LocalDateTime responseDate = LocalDateTime.now();
        LocalDateTime deadline = responseDate.plusDays(4);

        when(time.now()).thenReturn(responseDate);
        when(deadlinesCalculator.calculateApplicantResponseDeadline(any(LocalDateTime.class))).thenReturn(deadline);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(CREATE_CLAIM.name())
            .caseDetailsBefore(CaseDetails.builder().data(Map.of("state", "created")).build())
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondentResponseIsSame(NO)
            .respondent2SameLegalRepresentative(NO)
            .respondent1Copy(PartyBuilder.builder().individual().build())
            .respondent2Copy(PartyBuilder.builder().individual().build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BearerToken"))
            .request(callbackRequest)
            .build();

        callbackParams.getRequest().getCaseDetailsBefore().setState("AWAITING_RESPONDENT_ACKNOWLEDGEMENT");

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) setApplicantResponseDeadline.execute(callbackParams);

        assertEquals("AWAITING_APPLICANT_INTENTION", response.getState());

        verify(frcDocumentsUtils).assembleDefendantsFRCDocuments(caseData);
    }
}
