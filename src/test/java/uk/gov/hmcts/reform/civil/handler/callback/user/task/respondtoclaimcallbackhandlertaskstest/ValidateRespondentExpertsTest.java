package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertaskstest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.ValidateRespondentExperts;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class ValidateRespondentExpertsTest {

    @InjectMocks
    private ValidateRespondentExperts validateRespondentExperts;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private StateFlow mockedStateFlow;

    @Mock
    private UserService userService;

    @Test
    void shouldValidateRespondentExpertsWhenMultiPartyScenarioOneVOne() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1DQ(Respondent1DQ
                               .builder().respondent1DQExperts(Experts.builder().expertRequired(NO).build())
                               .build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentExperts.execute(callbackParams);

        assertEquals(true, response.getErrors().isEmpty());
        assertThat(response).isNotNull();

    }

    @Test
    void shouldValidateRespondent1ExpertsWhenSolicitorRepresentsOnlyRespondentOne() {

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
            .thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1DQ(Respondent1DQ
                               .builder().respondent1DQExperts(Experts.builder()
                                                                   .expertRequired(NO)
                                                                   .build()).build())
            .build().toBuilder().ccdCaseReference(1234L)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentExperts.execute(callbackParams);

        assertEquals(true, response.getErrors().isEmpty());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldValidateRespondent1ExpertsWhenSolicitorRepresentsOnlyOneORBothRespondent() {

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, true);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2DQ(Respondent2DQ
                               .builder().respondent2DQExperts(Experts.builder()
                                                                   .expertRequired(NO)
                                                                   .build()).build())
            .build().toBuilder().ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentExperts.execute(callbackParams);

        assertEquals(true, response.getErrors().isEmpty());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldValidateRespondent2ExpertsWhenRespondent2HasDifferentResponseAndExperts() {

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, false);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .multiPartyClaimOneDefendantSolicitor()
            .respondent2DQ(Respondent2DQ
                               .builder().respondent2DQExperts(Experts.builder().expertRequired(NO).build())
                               .build())
            .respondent2SameLegalRepresentative(YES)
            .respondentResponseIsSame(NO)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentExperts.execute(callbackParams);

        assertEquals(true, response.getErrors().isEmpty());
        assertThat(response).isNotNull();
    }

    @Test
    void shouldValidateRespondent1ExpertsByDefault() {

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1DQ(Respondent1DQ
                               .builder().respondent1DQExperts(Experts.builder().expertRequired(YES).build())
                               .build())
            .build().toBuilder().ccdCaseReference(1234L)
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentExperts.execute(callbackParams);

        assertEquals(false, response.getErrors().isEmpty());
        assertThat(response).isNotNull();
    }
}
