package uk.gov.hmcts.reform.civil.handler.callback.camunda.robotics;

import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.robotics.JsonSchemaValidationService;
import uk.gov.hmcts.reform.civil.service.robotics.RoboticsNotificationService;
import uk.gov.hmcts.reform.civil.service.robotics.exception.JsonSchemaValidationException;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistorySequencer;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapperForSpec;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isMultiPartyScenario;

@SpringBootTest(classes = {
    NotifyDefaultJudgmentHandler.class,
    JsonSchemaValidationService.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class,
    EventHistorySequencer.class,
    EventHistoryMapper.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    OrganisationService.class
})
@ExtendWith(SpringExtension.class)
public class NotifyDefaultJudgmentHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private RoboticsNotificationService roboticsNotificationService;

    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    IdamClient idamClient;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    LocationRefDataService locationRefDataService;
    @MockBean
    RoboticsDataMapperForSpec roboticsDataMapperForSpec;
    @MockBean
    private JsonSchemaValidationService validationService;
    @MockBean
    private Time time;
    @MockBean
    LocationRefDataUtil locationRefDataUtil;

    @Autowired
    private NotifyDefaultJudgmentHandler handler;

    @Nested
    class ValidJsonPayload {

        @Test
        void shouldNotifyRobotics_whenNoSchemaErrors() {
            when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            boolean multiPartyScenario = isMultiPartyScenario(caseData);
            handler.handle(params);

            verify(roboticsNotificationService).notifyRobotics(caseData, multiPartyScenario,
                                                               params.getParams().get(BEARER_TOKEN).toString());
        }

        @Test
        void shouldNotNotifyRobotics_whenLrDisabled() {
            when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build()
                .toBuilder().superClaimType(SuperClaimType.SPEC_CLAIM).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            boolean multiPartyScenario = isMultiPartyScenario(caseData);
        }

        @Test
        void shouldNotNotifyRobotics_whenRpaToggleOff() {
            // Given
            when(featureToggleService.isRPAEmailEnabled()).thenReturn(false);
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build()
                .toBuilder().superClaimType(SuperClaimType.SPEC_CLAIM).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            boolean multiPartyScenario = isMultiPartyScenario(caseData);

            // When
            handler.handle(params);

            // Then
            verify(roboticsNotificationService, times(0)).notifyRobotics(caseData, multiPartyScenario,
                                                                         params.getParams().get(BEARER_TOKEN).toString()
            );
        }

        @Test
        void shouldNotifyLiPJudgmentEmail_whenLiPDefendantAndPinEnabled() {
            //Given
            when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
            when(featureToggleService.isPinInPostEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build()
                .toBuilder().superClaimType(SuperClaimType.SPEC_CLAIM).respondent1Represented(YesOrNo.NO).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            //When
            handler.handle(params);

            //Then
            verify(roboticsNotificationService).notifyJudgementLip(caseData);
        }
    }

    @Nested
    class InValidJsonPayload {

        @Test
        void shouldThrowJsonSchemaValidationException_whenSchemaErrors() {
            when(featureToggleService.isRPAEmailEnabled()).thenReturn(true);
            when(validationService.validate(anyString())).thenReturn(Set.of(new ValidationMessage.Builder().build()));
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            assertThrows(
                JsonSchemaValidationException.class,
                () -> handler.handle(params)
            );
            verifyNoInteractions(roboticsNotificationService);
        }

    }
}
