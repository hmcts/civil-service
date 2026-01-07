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
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.ValidateUnavailableDates;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ValidateUnavailableDatesTest {

    @Mock
    private  UnavailableDateValidator unavailableDateValidator;

    @Mock
    private  IStateFlowEngine stateFlowEngine;

    @Mock
    private  CoreCaseUserService coreCaseUserService;

    @Mock
    private  UserService userService;

    @Mock
    private StateFlow mockedStateFlow;

    @InjectMocks
    private ValidateUnavailableDates validateUnavailableDates;

    @Test
    void shouldReturnNoError_whenUnavailableDateIsInTheFuture() {
        Hearing hearing = new Hearing();
        hearing.setUnavailableDatesRequired(YES);
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
        unavailableDate.setDate(LocalDate.now().plusDays(5));
        hearing.setUnavailableDates(wrapElements(unavailableDate));
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearing(hearing);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(respondent1DQ).build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoError_whenUnavailableDateIsInThePast() {
        Hearing hearing = new Hearing();
        hearing.setUnavailableDatesRequired(YES);
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
        unavailableDate.setDate(LocalDate.now().minusYears(5));
        hearing.setUnavailableDates(wrapElements(unavailableDate));
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearing(hearing);
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(respondent1DQ).build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(unavailableDateValidator.validate(any(Hearing.class)))
            .thenReturn(List.of("Unavailable date cannot be in the past"));

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).containsExactly("Unavailable date cannot be in the past");
    }

    @Test
    void shouldValidateRespondent2Hearing_whenSolicitorRepresentsOnlyRespondentTwo() {
        Hearing respondent2Hearing = new Hearing();
        respondent2Hearing.setUnavailableDatesRequired(YES);
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
        unavailableDate.setDate(LocalDate.now().plusDays(7));
        respondent2Hearing.setUnavailableDates(wrapElements(unavailableDate));

        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearing(respondent2Hearing);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1DQ(respondent1DQ)
            .build();
        caseData.setCcdCaseReference(1234L);

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, true);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldValidateRespondent1Hearing_whenMultiPartyScenarioIsOneVOne() {
        Hearing respondent1Hearing = new Hearing();
        respondent1Hearing.setUnavailableDatesRequired(YES);
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
        unavailableDate.setDate(LocalDate.now().plusDays(7));
        respondent1Hearing.setUnavailableDates(wrapElements(unavailableDate));

        Hearing respondent2Hearing = new Hearing();
        respondent2Hearing.setUnavailableDatesRequired(YES);
        UnavailableDate unavailableDate2 = new UnavailableDate();
        unavailableDate2.setUnavailableDateType(UnavailableDateType.SINGLE_DATE);
        unavailableDate2.setDate(LocalDate.now().plusDays(3));
        respondent2Hearing.setUnavailableDates(wrapElements(unavailableDate2));

        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQHearing(respondent1Hearing);
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQHearing(respondent2Hearing);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .build();
        caseData.setCcdCaseReference(1234L);
        caseData.setRespondent2SameLegalRepresentative(YES);
        caseData.setRespondentResponseIsSame(NO);

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, "BEARER TOKEN"))
            .build();

        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, false);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }
}

