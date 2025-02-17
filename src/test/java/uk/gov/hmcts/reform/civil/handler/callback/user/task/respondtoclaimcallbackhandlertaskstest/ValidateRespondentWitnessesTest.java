package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertaskstest;

import org.elasticsearch.core.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.ValidateRespondentWitnesses;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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
class ValidateRespondentWitnessesTest {

    @InjectMocks
    private ValidateRespondentWitnesses validateRespondentWitnesses;

    @Mock
    private IStateFlowEngine stateFlowEngine;

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Mock
    private StateFlow mockedStateFlow;

    @Mock
    private UserService userService;

    @Test
    void shouldValidateRespondentWitnessesWhenMultiPartyScenarioOneVOne() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent1DQ(Respondent1DQ
                               .builder().respondent1DQWitnesses(Witnesses.builder().build())
                               .build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertEquals(true, response.getErrors().isEmpty());
        assertThat(response).isNotNull();

    }

    @Test
    void shouldValidateRespondent1WitnessesWhenSolicitorRepresentsOnlyRespondentOne() {

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), eq(RESPONDENTSOLICITORONE)))
            .thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1DQ(Respondent1DQ
                               .builder().respondent1DQWitnesses(Witnesses.builder()
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
            (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldValidateRespondent1WitnessesWhenSolicitorRepresentsOnlyOneORBothRespondent() {

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, true);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent2DQ(Respondent2DQ
                               .builder().respondent2DQWitnesses(Witnesses.builder()
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
            (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldValidateRespondent2WitnessesWhenRespondent2HasDifferentResponseAndWitnesses() {

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, false);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .multiPartyClaimSameDefendantSolicitor()
            .respondent2DQ(Respondent2DQ
                               .builder().respondent2DQWitnesses(Witnesses.builder()
                                                                     .build())
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
            (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertThat(response).isNotNull();
    }

    @Test
    void shouldValidateRespondent1WitnessesByDefault() {

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1DQ(Respondent1DQ
                               .builder().respondent1DQWitnesses(Witnesses.builder().build())
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
            (AboutToStartOrSubmitCallbackResponse) validateRespondentWitnesses.execute(callbackParams);

        assertThat(response).isNotNull();
    }
}
