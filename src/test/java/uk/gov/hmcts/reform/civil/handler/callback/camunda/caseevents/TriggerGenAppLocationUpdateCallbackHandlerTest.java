package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GenAppStateHelperService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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

@ExtendWith(MockitoExtension.class)
class TriggerGenAppLocationUpdateCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    TriggerGenAppLocationUpdateCallbackHandler handler;

    @Mock
    private GenAppStateHelperService helperService;
    @Mock
    private FeatureToggleService featureToggleService;

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module());
        handler = new TriggerGenAppLocationUpdateCallbackHandler(helperService, featureToggleService, mapper);
        lenient().when(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(any())).thenReturn(true);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplication() {
            CaseData caseData = buildCaseDataWithApplications();
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any());
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verify(helperService, never()).triggerEvent(any(CaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplicationNotRepresented() {
            CaseData caseData = buildLipCaseDataWithApplications();
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any());
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplicationNotRepresentedAndNotInEa() {
            CaseData caseData = buildLipCaseDataWithApplications();
            when(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(any())).thenReturn(false);
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any());
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerGeneralApplicationEvent_whenCaseHasGeneralApplicationRepresentedAndNotInEa() {
            CaseData caseData = buildCaseDataWithApplications();
            when(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(any())).thenReturn(false);
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any());
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldSetEaFlagWhenConditionsMet() {
            CaseData caseData = buildLipCaseDataWithApplications();
            when(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(any())).thenReturn(false);
            when(featureToggleService.isCuiGaNroEnabled()).thenReturn(false);
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updated = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getGaEaCourtLocation()).isEqualTo(YesOrNo.YES);
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldNotSetEaFlagWhenNroEnabled() {
            CaseData caseData = buildLipCaseDataWithApplications();
            when(featureToggleService.isGaForLipsEnabledAndLocationWhiteListed(any())).thenReturn(false);
            when(featureToggleService.isCuiGaNroEnabled()).thenReturn(true);
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            CaseData updated = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updated.getGaEaCourtLocation()).isNull();
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldReturnErrorWhenTriggerFails() {
            CaseData caseData = buildCaseDataWithApplications();
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);
            when(helperService.triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE)))
                .thenThrow(new RuntimeException("Some Error"));

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_UPDATE_GA_LOCATION.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNotEmpty();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any());
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_LOCATION_UPDATE));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldTriggerReconfigureEventWhenRequested() {
            CaseData caseData = buildCaseDataWithApplications();
            when(helperService.updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any()))
                .thenReturn(caseData);

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder().eventId(TRIGGER_TASK_RECONFIG_GA.name()).build())
                .build();

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verify(helperService, times(1)).updateApplicationLocationDetailsInClaim(any(GeneralApplicationCaseData.class), any());
            verify(helperService, times(1)).triggerEvent(any(GeneralApplicationCaseData.class), eq(TRIGGER_TASK_RECONFIG));
            verifyNoMoreInteractions(helperService);
        }

        @Test
        void shouldNotTriggerEventWhenNoApplications() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000").region("2").build())
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isNull();
            verifyNoInteractions(helperService);
        }
    }

    @Test
    void handleEventsReturnsExpectedEvents() {
        assertThat(handler.handledEvents()).contains(TRIGGER_UPDATE_GA_LOCATION);
        assertThat(handler.handledEvents()).contains(TRIGGER_TASK_RECONFIG_GA);
    }

    private CaseData buildCaseDataWithApplications() {
        return GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithDetails(CaseData.builder().build(),
                true,
                true,
                true, true,
                exampleStatusMap());
    }

    private CaseData buildLipCaseDataWithApplications() {
        return GeneralApplicationDetailsBuilder.builder()
            .getTestCaseDataWithLocationDetailsLip(CaseData.builder().build(),
                true,
                true,
                true, true,
                exampleStatusMap());
    }

    private Map<String, String> exampleStatusMap() {
        Map<String, String> latestStatus = new HashMap<>();
        latestStatus.put("1234", "Application Submitted - Awaiting Judicial Decision");
        latestStatus.put("2345", "Order Made");
        return latestStatus;
    }
}
