package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RespondToClaimSpecCallbackHandler.class,
        ExitSurveyConfiguration.class,
        ExitSurveyContentService.class,
        JacksonAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        DateOfBirthValidator.class,
        UnavailableDateValidator.class,
        CaseDetailsConverter.class,
        LocationReferenceDataService.class,
        CourtLocationUtils.class,
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        AssignCategoryId.class,
        FrcDocumentsUtils.class,
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
class RespondToClaimSpecCallbackHandlerHandleLocationsTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    private Time time;

    @Mock
    private StateFlow mockedStateFlow;

    @MockBean
    private SimpleStateFlowEngine stateFlowEngine;

    @MockBean
    private CourtLocationUtils courtLocationUtils;

    private DynamicList preferredCourt;
    private LocationRefData completePreferredLocation;
    private StateFlow flow;
    private Party defendant1;

    @BeforeEach
    void setUp() {
        DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
        preferredCourt = DynamicList.builder()
                .listItems(locationValues.getListItems())
                .value(locationValues.getListItems().get(0))
                .build();
        defendant1 = Party.builder()
                .type(Party.Type.COMPANY)
                .companyName("company")
                .build();
        List<LocationRefData> locations = List.of(LocationRefData.builder().build());
        completePreferredLocation = LocationRefData.builder()
                .regionId("regionId")
                .epimmsId("epimms")
                .courtLocationCode("code")
                .build();
        flow = mock(StateFlow.class);
        UserInfo userInfo = UserInfo.builder().uid("798").build();

        when(locationRefDataService.getCourtLocationsForDefaultJudgments(any())).thenReturn(locations);
        when(courtLocationUtils.findPreferredLocationData(any(), any())).thenReturn(completePreferredLocation);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(flow);
        when(flow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(coreCaseUserService.userHasCaseRole(anyString(), anyString(), any(CaseRole.class))).thenReturn(true);
        when(userService.getUserInfo(anyString())).thenReturn(userInfo);
        when(dqResponseDocumentUtils.buildDefendantResponseDocuments(any(CaseData.class)))
                .thenReturn(Collections.emptyList());
    }


    private CallbackParams createParams(CaseData caseData) {
        return callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
    }

    @Test
    void oneVOne() {
        CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .ccdCaseReference(354L)
                .respondent1(defendant1)
                .respondent1Copy(defendant1)
                .respondent1DQ(
                        Respondent1DQ.builder()
                                .respondToCourtLocation(
                                        RequestedCourt.builder()
                                                .responseCourtLocations(preferredCourt)
                                                .reasonForHearingAtSpecificCourt("Reason")
                                                .build()
                                )
                                .build()
                )
                .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1
                ))
                .build();
        CallbackParams params = createParams(caseData);
        when(flow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        AbstractObjectAssert<?, ?> sent1 = assertThat(response.getData())
                .extracting("respondent1DQRequestedCourt");
        sent1.extracting("caseLocation")
                .extracting("region")
                .isEqualTo(completePreferredLocation.getRegionId());
        sent1.extracting("caseLocation")
                .extracting("baseLocation")
                .isEqualTo(completePreferredLocation.getEpimmsId());
        sent1.extracting("responseCourtCode")
                .isEqualTo(completePreferredLocation.getCourtLocationCode());
        sent1.extracting("reasonForHearingAtSpecificCourt")
                .isEqualTo("Reason");

        verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
    }

    @Test
    void oneVTwo_SecondDefendantRepliesSameLegalRep() {
        CaseData caseData = CaseData.builder()
                .respondent2SameLegalRepresentative(YES)
                .caseAccessCategory(SPEC_CLAIM)
                .ccdCaseReference(354L)
                .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
                .respondent2ClaimResponseTypeForSpec(FULL_ADMISSION)
                .respondent1(defendant1)
                .respondent1Copy(defendant1)
                .respondent1DQ(
                        Respondent1DQ.builder()
                                .respondToCourtLocation(
                                        RequestedCourt.builder()
                                                .responseCourtLocations(preferredCourt)
                                                .reasonForHearingAtSpecificCourt("Reason")
                                                .build()
                                )
                                .build()
                )
                .respondent2DQ(
                        Respondent2DQ.builder()
                                .respondToCourtLocation2(
                                        RequestedCourt.builder()
                                                .responseCourtLocations(preferredCourt)
                                                .reasonForHearingAtSpecificCourt("Reason123")
                                                .build()
                                )
                                .build()
                )
                .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                ))
                .build();
        CallbackParams params = createParams(caseData);
        when(flow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        LocalDate whenWillPay = LocalDate.now().plusDays(5);
        given(deadlineExtensionCalculatorService.calculateExtendedDeadline(any(), anyInt())).willReturn(whenWillPay);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        AbstractObjectAssert<?, ?> sent2 = assertThat(response.getData())
                .extracting("respondent2DQRequestedCourt");
        sent2.extracting("caseLocation")
                .extracting("region")
                .isEqualTo(completePreferredLocation.getRegionId());
        sent2.extracting("caseLocation")
                .extracting("baseLocation")
                .isEqualTo(completePreferredLocation.getEpimmsId());
        sent2.extracting("responseCourtCode")
                .isEqualTo(completePreferredLocation.getCourtLocationCode());
        sent2.extracting("reasonForHearingAtSpecificCourt")
                .isEqualTo("Reason123");

        verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
    }

    @Test
    void oneVTwo_SecondDefendantReplies() {
        CaseData caseData = CaseData.builder()
                .caseAccessCategory(SPEC_CLAIM)
                .ccdCaseReference(354L)
                .respondent1(defendant1)
                .respondent1Copy(defendant1)
                .respondent1DQ(
                        Respondent1DQ.builder()
                                .respondToCourtLocation(
                                        RequestedCourt.builder()
                                                .responseCourtLocations(preferredCourt)
                                                .reasonForHearingAtSpecificCourt("Reason")
                                                .build()
                                )
                                .build()
                )
                .respondent2DQ(
                        Respondent2DQ.builder()
                                .respondToCourtLocation2(
                                        RequestedCourt.builder()
                                                .responseCourtLocations(preferredCourt)
                                                .reasonForHearingAtSpecificCourt("Reason123")
                                                .build()
                                )
                                .build()
                )
                .showConditionFlags(EnumSet.of(
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1,
                        DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2
                ))
                .build();
        CallbackParams params = createParams(caseData);
        when(flow.isFlagSet(FlowFlag.TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        AbstractObjectAssert<?, ?> sent2 = assertThat(response.getData())
                .extracting("respondent2DQRequestedCourt");
        sent2.extracting("caseLocation")
                .extracting("region")
                .isEqualTo(completePreferredLocation.getRegionId());
        sent2.extracting("caseLocation")
                .extracting("baseLocation")
                .isEqualTo(completePreferredLocation.getEpimmsId());
        sent2.extracting("responseCourtCode")
                .isEqualTo(completePreferredLocation.getCourtLocationCode());
        sent2.extracting("reasonForHearingAtSpecificCourt")
                .isEqualTo("Reason123");

        verify(dqResponseDocumentUtils, times(1)).buildDefendantResponseDocuments(any(CaseData.class));
    }
}
