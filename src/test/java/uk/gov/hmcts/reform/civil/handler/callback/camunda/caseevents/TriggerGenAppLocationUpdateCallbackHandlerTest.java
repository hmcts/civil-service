package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_LOCATION_UPDATE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_TASK_RECONFIG_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIGGER_UPDATE_GA_LOCATION;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)

 class TriggerGenAppLocationUpdateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    TriggerGenAppLocationUpdateCallbackHandler handler;

    @Mock
    private GenAppStateHelperService helperService;
    @Mock
    private FeatureToggleService featureToggleService;
    private ObjectMapper mapper;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        public void before() {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            handler = new TriggerGenAppLocationUpdateCallbackHandler(helperService, featureToggleService, mapper);

            when(featureToggleService.isLocationWhiteListed(any())).thenReturn(true);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseDataBuilder.builder().build(),
                                            true,
                                            true,
                                            true, true,
                                            getOriginalStatusOfGeneralApplication()
                );

            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplicationNotRepresented() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithLocationDetailsLip(CaseDataBuilder.builder().build(),
                                            true,
                                            true,
                                            true, true,
                                            getOriginalStatusOfGeneralApplication()
                );

            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplicationNotRepresentedAndNotInEa() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithLocationDetailsLip(CaseDataBuilder.builder().build(),
                                                       true,
                                                       true,
                                                       true, true,
                                                       getOriginalStatusOfGeneralApplication()
                );
            when(featureToggleService.isLocationWhiteListed(any())).thenReturn(false);
            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplicationRepresentedAndNotInEa() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseDataBuilder.builder().build(),
                                                       true,
                                                       true,
                                                       true, true,
                                                       getOriginalStatusOfGeneralApplication()
                );
            when(featureToggleService.isLocationWhiteListed(any())).thenReturn(false);
            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldSetTheEaFlagToTriggerTheWATask_TakeCaseOffline() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                new GeneralApplication()
                    .setCaseLink(new CaseLink().setCaseReference("54326781"))
                    .setGeneralAppType(new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT))));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
                .caseDataLip(new CaseDataLiP().setApplicant1SettleClaim(YesOrNo.YES))
                .respondent1Represented(YesOrNo.NO).build();
            caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("000000").setRegion("2"))
                .setGeneralApplications(gaApplications);
            when(featureToggleService.isLocationWhiteListed(any())).thenReturn(false);
            when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);
            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getGaEaCourtLocation().equals(YesOrNo.YES)).isTrue();
            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldNotSetTheEaFlagToTriggerTheWATask_TakeCaseOffline() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                new GeneralApplication()
                    .setCaseLink(new CaseLink().setCaseReference("54326781"))
                    .setGeneralAppType(new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT))));
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmittedSmallClaim()
                .caseDataLip(new CaseDataLiP().setApplicant1SettleClaim(YesOrNo.YES))
                .respondent1Represented(YesOrNo.NO).build();
            caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("000000").setRegion("2"))
                .setGeneralApplications(gaApplications);
            when(featureToggleService.isLocationWhiteListed(any())).thenReturn(false);
            when(featureToggleService.isCuiGaNroEnabled()).thenReturn(true);
            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_UPDATE_GA_LOCATION.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedData.getGaEaCourtLocation()).isNull();
            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(), any());
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_LOCATION_UPDATE);
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldNotTriggerGeneralApplicationEvent_whenCaseHasNoGeneralApplicationLip() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .caseManagementLocation(
                CaseLocationCivil.builder().baseLocation("00000")
                    .region("2").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(helperService);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldNotTriggerGeneralApplicationEvent_whenCaseHasNoGeneralApplication() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().caseManagementLocation(
                CaseLocationCivil.builder().baseLocation("00000")
                    .region("2").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verifyNoInteractions(helperService);
            assertThat(response.getErrors()).isNull();
        }

        @Test
        void shouldTriggerCivilServiceEvent_whenLocationIsNotInEaRegion() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssuedUnrepresentedDefendant().caseManagementLocation(
                CaseLocationCivil.builder().baseLocation("00000")
                    .region("2").build()).build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            when(featureToggleService.isLocationWhiteListed(any())).thenReturn(false);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
        }

        @Test
        void triggerGeneralApplicationEventThrowsException_HandleFailure() {
            CaseData baseCaseData = CaseDataBuilder.builder().build();
            baseCaseData.setCcdCaseReference(1234L);
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(baseCaseData,
                                            true,
                                            true,
                                            true, true,
                                            getOriginalStatusOfGeneralApplication()
                );
            String expectedErrorMessage = "Could not trigger event to update location on application under case: "
                + caseData.getCcdCaseReference();
            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotNull();
            assertThat(response.getErrors()).contains(expectedErrorMessage);
        }

        @Test
        void shouldTriggerReconfigureWhenCallbackEventIsReconfigGA() {
            CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseDataBuilder.builder().build(),
                                            true,
                                            true,
                                            true, true,
                                            getOriginalStatusOfGeneralApplication()
                );
            when(helperService.updateApplicationLocationDetailsInClaim(any(), any())).thenReturn(caseData);
            CallbackParams callbackParams = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(TRIGGER_TASK_RECONFIG_GA.name())
                             .build())
                .build();
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(callbackParams);
            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).triggerEvent(caseData, TRIGGER_TASK_RECONFIG);
        }

        private Map<String, String> getOriginalStatusOfGeneralApplication() {
            Map<String, String> latestStatus = new HashMap<>();
            latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
            latestStatus.put("2345", "Order Made");
            return latestStatus;
        }
    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(TRIGGER_UPDATE_GA_LOCATION);
        assertThat(handler.handledEvents()).contains(TRIGGER_TASK_RECONFIG_GA);
    }

}
