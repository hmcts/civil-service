package uk.gov.hmcts.reform.civil;

import au.com.dius.pact.consumer.PactVerificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.config.PrdAdminUserConfiguration;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataMaxEdgeCasesBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataMinEdgeCasesBuilder;
import uk.gov.hmcts.reform.civil.sendgrid.SendGridClient;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.EventHistorySequencer;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsDataMapper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.prd.client.OrganisationApi;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.CONTEST_JURISDICTION;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.PART_DEFENCE;
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
class RpaConsumerTest extends BaseRpaTest {

    @Autowired
    RoboticsDataMapper roboticsDataMapper;

    @MockBean
    SendGridClient sendGridClient;
    @MockBean
    OrganisationApi organisationApi;
    @MockBean
    AuthTokenGenerator authTokenGenerator;
    @MockBean
    IdamClient idamClient;
    @MockBean
    PrdAdminUserConfiguration userConfig;
    @MockBean
    FeatureToggleService featureToggleService;
    @MockBean
    private Time time;

    LocalDateTime localDateTime;

    @BeforeEach
    void setUp() {
        localDateTime = LocalDateTime.of(2020, 8, 1, 12, 0, 0);
        when(time.now()).thenReturn(localDateTime);
        given(organisationApi.findOrganisationById(any(), any(), any())).willReturn(ORGANISATION);
    }

    @Nested
    class UnrepresentedDefendant {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT)
                .legacyCaseReference("000DC001")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendant_WithMaximumData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendantMaximumData()
                .legacyCaseReference("000DC002")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant - max limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendant_WithMinimumData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendantMinimumData()
                .legacyCaseReference("000DC003")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant - minimal limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class UnregisteredDefendant {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT)
                .legacyCaseReference("000DC004")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendant_withMinimalData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateProceedsOfflineUnrepresentedDefendantWithMinimalData()
                .legacyCaseReference("000DC005")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant - minimal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendantWithMinimalData_withMaximalData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateProceedsOfflineUnregisteredDefendantMaximumData()
                .legacyCaseReference("000DC006")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant - maximal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class UnrepresentedAndUnregisteredDefendant {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedAndUnregisteredDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT)
                .legacyCaseReference("000DC038")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented and unregistered defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class PartAdmissionResponse {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithPartAdmission() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentPartAdmissionAfterNotificationAcknowledgement()
                .legacyCaseReference("000DC007")
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with part admission";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithPartAdmission_withMinimalData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateRespondentRespondToClaimWithMinimalData(PART_ADMISSION)
                .legacyCaseReference("000DC008")
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with part admission - minimal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithPartAdmission_withMaximalData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateRespondentRespondToClaimWithMaximalData(PART_ADMISSION)
                .legacyCaseReference("000DC009")
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with part admission - maximal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class FullAdmissionResponse {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullAdmissionAfterNotificationAcknowledged()
                .legacyCaseReference("000DC010")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full admission";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithFullAdmission_withMinimalData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateRespondentRespondToClaimWithMinimalData(FULL_ADMISSION)
                .legacyCaseReference("000DC011")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full admission - minimal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithFullAdmission_withMaximalData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateRespondentRespondToClaimWithMaximalData(FULL_ADMISSION)
                .legacyCaseReference("000DC012")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full admission - maximal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class CounterClaimResponse {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithCounterClaim() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentCounterClaim()
                .legacyCaseReference("000DC013")
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with counter claim";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithCounterClaim_withMinimalData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateRespondentRespondToClaimWithMinimalData(COUNTER_CLAIM)
                .legacyCaseReference("000DC014")
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with counter claim - minimal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithCounterClaim_withMaximalData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateRespondentRespondToClaimWithMaximalData(COUNTER_CLAIM)
                .legacyCaseReference("000DC015")
                .respondent1ClaimResponseIntentionType(CONTEST_JURISDICTION)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with counter claim - maximal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class FullDefenceNotProceed {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceNotProceeds() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_NOT_PROCEED)
                .legacyCaseReference("000DC016")
                .respondent1ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for applicant responded with confirms not to proceeds";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceNotProceeds_withMinimalData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateApplicantRespondToDefenceAndNotProceedMinimumData()
                .legacyCaseReference("000DC017")
                .respondent1ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for applicant responded with not to proceeds - minimal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceNotProceeds_withMaximalData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateApplicantRespondToDefenceAndNotProceedMaximumData()
                .legacyCaseReference("000DC018")
                .respondent1ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for applicant responded with not to proceeds - maximal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class FullDefenceProceed {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceProceeds() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.FULL_DEFENCE_PROCEED)
                .legacyCaseReference("000DC019")
                .respondent1ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for applicant responded with confirms to proceeds";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceProceeds_withMinimalData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .legacyCaseReference("000DC020")
                .respondent1ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for applicant responded with confirms to proceeds - minimal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenFullDefenceProceeds_withMaximalData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateApplicantRespondToDefenceAndProceed()
                .legacyCaseReference("000DC036")
                .respondent1ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for applicant responded with confirms to proceeds - maximal data";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class TakenOfflineByStaff {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterClaimIssue() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaff()
                .legacyCaseReference("000DC021")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after claim issue";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterClaimNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimNotified()
                .legacyCaseReference("000DC022")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after claim notified";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateTakenOfflineByStaffAfterClaimDetailsNotified()
                .legacyCaseReference("000DC023")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after claim details notified";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension()
                .legacyCaseReference("000DC024")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after claim details notified extension";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledged()
                .legacyCaseReference("000DC025")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after notification acknowledged";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterNotificationAcknowledgeExtension() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension()
                .legacyCaseReference("000DC026")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after notification acknowledge extension";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenCaseTakenOfflineAfterDefendantResponse() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateTakenOfflineByStaffAfterDefendantResponse()
                .legacyCaseReference("000DC027")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim taken offline after full defence";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class ClaimDismissedPastClaimDismissedDeadline {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDeadlinePassedAfterStateClaimDetailsNotified() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDismissed()
                .legacyCaseReference("000DC028")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Claim dismissed passed deadline after claim notification";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedTimeExtension()
                .claimDismissedDate(LocalDateTime.now())
                .legacyCaseReference("000DC029")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Claim dismissed passed deadline after notification then time extension";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDeadlinePassedAfterStateNotificationAcknowledged() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .claimDismissedDate(LocalDateTime.now())
                .legacyCaseReference("000DC030")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Claim dismissed passed deadline after notification acknowledged";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .claimDismissedDate(LocalDateTime.now())
                .legacyCaseReference("000DC031")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Claim dismissed passed deadline after notification acknowledged then time extension";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class PastApplicantResponseDeadline {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenApplicantResponseDeadlinePassedAfterFullDefence() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledgedTimeExtension()
                .atState(FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE)
                .legacyCaseReference("000DC032")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Claim taken offline passed applicant deadline after full defence response";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class CreateClaimRpaContinuousFeed {

        @BeforeEach
        void setup() {
            when(featureToggleService.isRpaContinuousFeedEnabled()).thenReturn(true);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.CLAIM_ISSUED)
                .legacyCaseReference("000DC033")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim issued in CCD";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendant_WithMaximumData() {
            CaseData caseData = CaseDataMaxEdgeCasesBuilder.builder()
                .atStateClaimIssuedMaximumData()
                .legacyCaseReference("000DC034")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim issued in CCD - max limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnregisteredDefendantWithMinimumData() {
            CaseData caseData = CaseDataMinEdgeCasesBuilder.builder()
                .atStateClaimIssuedWithMinimalData()
                .legacyCaseReference("000DC035")
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim issued in CCD - min limit";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }
}
