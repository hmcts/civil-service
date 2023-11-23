package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.PactVerificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisation;
import uk.gov.hmcts.reform.ccd.model.PreviousOrganisationCollectionItem;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.prd.client.OrganisationApi;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistorySequencer;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.matcher.IsValidJson.validateJson;

@Slf4j
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistorySequencer.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class,
    AddressLinesMapper.class,
    OrganisationService.class
})
class RpaCaseHandedOfflineConsumerTest extends BaseRpaTest {

    @Autowired
    RoboticsDataMapper roboticsDataMapper;

    @MockBean
    SendGridClient sendGridClient;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    UserService userService;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    LocationRefDataService locationRefDataService;
    @MockBean
    LocationRefDataUtil locationRefDataUtil;
    @MockBean
    private Time time;

    private static final String BEARER_TOKEN = "Bearer Token";

    LocalDateTime localDateTime;

    @BeforeEach
    void setUp() {
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
    }

    @Test
    @SneakyThrows
    void shouldGeneratePact_whenCaseTakenOffline() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atState(FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED)
            .legacyCaseReference("100DC001")
            .build();
        String payload = roboticsDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();

        System.out.println("PAYLOAD");
        System.out.println(payload);

        assertThat(payload, validateJson());

        PactVerificationResult result = getPactVerificationResult(payload);

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }

    @Test
    @SneakyThrows
    void shouldGeneratePact_whenCaseDismissed() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atState(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE)
            .legacyCaseReference("100DC001")
            .build();
        String payload = roboticsDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();

        System.out.println("PAYLOAD");
        System.out.println(payload);

        assertThat(payload, validateJson());

        PactVerificationResult result = getPactVerificationResult(payload);

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }

    @Test
    @SneakyThrows
    void shouldGeneratePact_whenNoticeOfChangeAndCaseTakenOffline() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atState(FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED)
            .legacyCaseReference("100DC001")
            .applicant1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("QWERTY R").build())
                    .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                    .previousOrganisations(List.of(
                        PreviousOrganisationCollectionItem.builder().value(
                            PreviousOrganisation.builder()
                                .organisationName("app 1 org")
                                .toTimestamp(LocalDateTime.parse("2022-02-01T12:00:00.000550439"))
                                .build()).build()))
                    .build())
            .build();
        String payload = roboticsDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();

        System.out.println("PAYLOAD");
        System.out.println(payload);

        assertThat(payload, validateJson());

        PactVerificationResult result = getPactVerificationResult(payload);

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }

    @Test
    @SneakyThrows
    void shouldGeneratePact_whenNoticeOfChangeAndCaseDismissed() {
        when(featureToggleService.isNoticeOfChangeEnabled()).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder()
            .atState(FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE)
            .legacyCaseReference("100DC001")
            .applicant1OrganisationPolicy(
                OrganisationPolicy.builder()
                    .organisation(Organisation.builder().organisationID("QWERTY R").build())
                    .orgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                    .previousOrganisations(List.of(
                        PreviousOrganisationCollectionItem.builder().value(
                            PreviousOrganisation.builder()
                                .organisationName("app 1 org")
                                .toTimestamp(LocalDateTime.parse("2022-02-01T12:00:00.000550439"))
                                .build()).build()))
                    .build())
            .build();
        String payload = roboticsDataMapper.toRoboticsCaseData(caseData, BEARER_TOKEN).toJsonString();

        System.out.println("PAYLOAD");
        System.out.println(payload);

        assertThat(payload, validateJson());

        PactVerificationResult result = getPactVerificationResult(payload);

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }

}
