package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertaskstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.PopulateRespondentCopyObjects;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;

@ExtendWith(MockitoExtension.class)
class PopulateRespondentCopyObjectsTest  {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourtLocationUtils courtLocationUtils;

    @Mock
    private LocationReferenceDataService locationReferenceDataService;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private UserService userService;

    @Mock
    private StateFlow mockedStateFlow;

    @InjectMocks
    private PopulateRespondentCopyObjects populateRespondentCopyObjects;

    @BeforeEach
    public void setup() {

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
    }

    @Test
    void shouldPopulateRespondentCopyObjects() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotified()
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) populateRespondentCopyObjects.execute(callbackParams);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnError_whenRespondent1HasAlreadySubmittedResponse() {

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimSameDefendantSolicitor()
            .respondent1ResponseDate(LocalDateTime.now())
            .build().toBuilder().ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) populateRespondentCopyObjects.execute(callbackParams);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .containsExactly("There is a problem\nYou have already submitted the defendant's response");
    }

    @Test
    void shouldReturnError_whenRespondent2HasAlreadySubmittedResponse() {

        CaseData caseData = CaseDataBuilder.builder()
            .respondent2ResponseDate(LocalDateTime.now())
            .build().toBuilder().ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) populateRespondentCopyObjects.execute(callbackParams);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .containsExactly("There is a problem\nYou have already submitted the defendant's response");
    }

    @Test
    void shouldReturnErrorIfRespondent1SubmissionPassedDeadline() {
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(3);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1ResponseDeadline(pastDeadline)
            .build().toBuilder().ccdCaseReference(1234L)
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(true);

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) populateRespondentCopyObjects.execute(callbackParams);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .containsExactly("You cannot submit a response now as you have passed your deadline");
    }

    @Test
    void shouldReturnErrorIfRespondent2SubmissionPassedDeadline() {
        LocalDateTime pastDeadline = LocalDateTime.now().minusDays(3);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent2ResponseDeadline(pastDeadline)
            .build().toBuilder().ccdCaseReference(1234L)
            .build();

        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORONE))).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) populateRespondentCopyObjects.execute(callbackParams);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors())
            .containsExactly("You cannot submit a response now as you have passed your deadline");
    }
}
