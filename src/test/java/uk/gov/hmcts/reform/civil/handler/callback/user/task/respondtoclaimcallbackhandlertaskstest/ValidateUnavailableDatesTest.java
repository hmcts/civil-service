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
        Hearing hearing = Hearing.builder()
            .unavailableDatesRequired(YES)
            .unavailableDates(wrapElements(UnavailableDate.builder()
                                               .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                               .date(LocalDate.now().plusDays(5))
                                               .build()))
            .build();
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQHearing(hearing).build()).build();

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
        Hearing hearing = Hearing.builder()
            .unavailableDatesRequired(YES)
            .unavailableDates(wrapElements(UnavailableDate.builder()
                                               .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                               .date(LocalDate.now().minusYears(5))
                                               .build()))
            .build();
        CaseData caseData = CaseDataBuilder.builder()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQHearing(hearing).build()).build();

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
        Hearing respondent2Hearing = Hearing.builder()
            .unavailableDatesRequired(YES)
            .unavailableDates(wrapElements(UnavailableDate.builder()
                                               .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                               .date(LocalDate.now().plusDays(7))
                                               .build()))
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQHearing(respondent2Hearing)
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

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, true);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldValidateRespondent1Hearing_whenMultiPartyScenarioIsOneVOne() {
        Hearing respondent1Hearing = Hearing.builder()
            .unavailableDatesRequired(YES)
            .unavailableDates(wrapElements(UnavailableDate.builder()
                                               .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                               .date(LocalDate.now().plusDays(7))
                                               .build()))
            .build();

        Hearing respondent2Hearing = Hearing.builder()
            .unavailableDatesRequired(YES)
            .unavailableDates(wrapElements(UnavailableDate.builder()
                                               .unavailableDateType(UnavailableDateType.SINGLE_DATE)
                                               .date(LocalDate.now().plusDays(3))
                                               .build()))
            .build();

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQHearing(respondent1Hearing)
                               .build())
            .respondent2DQ(Respondent2DQ.builder()
                               .respondent2DQHearing(respondent2Hearing)
                               .build())
            .build().toBuilder().ccdCaseReference(1234L)
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

        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class)))
            .thenReturn(false, false);

        AboutToStartOrSubmitCallbackResponse response =
            (AboutToStartOrSubmitCallbackResponse) validateUnavailableDates.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }
}

