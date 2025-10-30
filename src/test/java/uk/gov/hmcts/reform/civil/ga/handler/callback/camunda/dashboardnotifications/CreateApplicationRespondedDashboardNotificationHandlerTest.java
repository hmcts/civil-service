package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_APPLICATION_RESPONDED_DASHBOARD_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SpringBootTest(classes = {
    CaseDetailsConverter.class,
    CreateApplicationRespondedDashboardNotificationHandler.class,
    JacksonAutoConfiguration.class,
},
    properties = {"reference.database.enabled=false"})
public class CreateApplicationRespondedDashboardNotificationHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private DocUploadDashboardNotificationService dashboardNotificationService;
    @MockBean
    IdamClient idamClient;
    @MockBean
    GaForLipService gaForLipService;
    @Autowired
    CreateApplicationRespondedDashboardNotificationHandler handler;

    private static final String CAMUNDA_EVENT = "INITIATE_GENERAL_APPLICATION";
    private static final String BUSINESS_PROCESS_INSTANCE_ID = "11111";
    private static final String ACTIVITY_ID = "anyActivity";
    private static final String APP_UID = "9";
    private static final String DEF_UID = "10";
    private static final String DEF2_UID = "11";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_APPLICATION_RESPONDED_DASHBOARD_NOTIFICATION);
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLip() {
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        CallbackParams params = callbackParamsOf(getCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("RESPONDENT"), anyString());
        verify(dashboardNotificationService).createResponseDashboardNotification(any(), eq("APPLICANT"), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLipVLip() {
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        verify(dashboardNotificationService, times(2))
            .createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLRvLR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(false);
        when(gaForLipService.isLipApp(any())).thenReturn(false);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verifyNoInteractions(dashboardNotificationService);

        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLipVLR() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService).createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    @Test
    void buildResponseConfirmationReturnsCorrectMessageWhenGaHasLipAndVaryJudgeApppLRVLip() {
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        CallbackParams params = callbackParamsOf(getVaryCase(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION),
                                                 CallbackType.ABOUT_TO_SUBMIT);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(dashboardNotificationService).createOfflineResponseDashboardNotification(any(), any(), anyString());
        assertThat(response).isNotNull();
    }

    private GeneralApplicationCaseData getVaryCase(CaseState state) {
        List<GeneralApplicationTypes> types = List.of(
            (VARY_PAYMENT_TERMS_OF_JUDGMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .hearingPreferredLocation(dynamicListTest)
                                        .hearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                        .build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .parentClaimantIsApplicant(NO)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email("abc@gmail.com").id(APP_UID).build())
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent(CAMUNDA_EVENT)
                                 .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .status(BusinessProcessStatus.STARTED)
                                 .activityId(ACTIVITY_ID)
                                 .build())
            .ccdState(state)
            .build();
    }

    private List<Element<GASolicitorDetailsGAspec>> getRespondentSolicitors() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id(DEF_UID)
            .email("test@gmail.com").organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id(DEF2_UID)
            .email("test@gmail.com").organisationIdentifier("org3").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        return respondentSols;
    }

    private GeneralApplicationCaseData getCase(CaseState state) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        DynamicList dynamicListTest = fromList(getSampleCourLocations());
        Optional<DynamicListElement> first = dynamicListTest.getListItems().stream().findFirst();
        first.ifPresent(dynamicListTest::setValue);

        return GeneralApplicationCaseData.builder()
            .generalAppRespondent1Representative(
                GARespondentRepresentative.builder()
                    .generalAppRespondent1Representative(YES)
                    .build())
            .defendant2PartyName("Defendant Two")
            .defendant1PartyName("Defendant One")
            .claimant1PartyName("Claimant One")
            .claimant2PartyName("Claimant Two")
            .judicialListForHearing(GAJudgesHearingListGAspec.builder()
                                        .hearingPreferredLocation(dynamicListTest)
                                        .hearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                        .build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(dynamicListTest)
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .build())
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .parentClaimantIsApplicant(NO)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("123").build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email("abc@gmail.com").id(APP_UID).build())
            .generalAppRespondentSolicitors(getRespondentSolicitors())
            .businessProcess(BusinessProcess
                                 .builder()
                                 .camundaEvent(CAMUNDA_EVENT)
                                 .processInstanceId(BUSINESS_PROCESS_INSTANCE_ID)
                                 .status(BusinessProcessStatus.STARTED)
                                 .activityId(ACTIVITY_ID)
                                 .build())
            .ccdState(state)
            .build();
    }

    protected List<String> getSampleCourLocations() {
        return new ArrayList<>(Arrays.asList("ABCD - RG0 0AL", "PQRS - GU0 0EE", "WXYZ - EW0 0HE", "LMNO - NE0 0BH"));
    }

}
