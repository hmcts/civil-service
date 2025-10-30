package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.ClaimantResponseOnCourtDecisionType;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.flowstate.TransitionsTestConfiguration;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.support.CaseDataNormalizer;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.AcknowledgementOfServiceStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.BreathingSpaceEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseProceedsInCasemanStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseQueriesStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDetailsNotifiedEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastDeadlineStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastNotificationsStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimIssuedEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimNotifiedEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.DefaultJudgmentEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.DefendantNoCDeadlineStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.MediationEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentLitigationFriendStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentFullAdmissionStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentFullDefenceStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentPartAdmissionStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentCounterClaimStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimDetailsNotifiedStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimNotifiedStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflinePastApplicantResponseStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineByStaffEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.UnregisteredDefendantStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedAndUnregisteredDefendantStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedDefendantStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.SpecRejectRepaymentPlanStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsManualOfflineSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.LocationRefDataUtil;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    uk.gov.hmcts.reform.civil.config.JacksonConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowBuilder.class,
    SimpleStateFlowEngine.class,
    TransitionsTestConfiguration.class,
    EventHistorySequencer.class,
    EventHistoryMapper.class,
    RoboticsTimelineHelper.class,
    RoboticsEventTextFormatter.class,
    RoboticsPartyLookup.class,
    RoboticsRespondentResponseSupport.class,
    RoboticsSequenceGenerator.class,
    ClaimIssuedEventStrategy.class,
    ClaimDetailsNotifiedEventStrategy.class,
    ClaimNotifiedEventStrategy.class,
    BreathingSpaceEventStrategy.class,
    ClaimDismissedPastNotificationsStrategy.class,
    ClaimDismissedPastDeadlineStrategy.class,
    AcknowledgementOfServiceStrategy.class,
    RespondentLitigationFriendStrategy.class,
    CaseQueriesStrategy.class,
    UnrepresentedDefendantStrategy.class,
    UnregisteredDefendantStrategy.class,
    UnrepresentedAndUnregisteredDefendantStrategy.class,
    TakenOfflineAfterClaimDetailsNotifiedStrategy.class,
    TakenOfflineAfterClaimNotifiedStrategy.class,
    TakenOfflinePastApplicantResponseStrategy.class,
    TakenOfflineByStaffEventStrategy.class,
    DefaultJudgmentEventStrategy.class,
    ConsentExtensionEventStrategy.class,
    GeneralApplicationStrikeOutStrategy.class,
    ClaimantResponseStrategy.class,
    CaseProceedsInCasemanStrategy.class,
    DefendantNoCDeadlineStrategy.class,
    MediationEventStrategy.class,
    RespondentFullDefenceStrategy.class,
    RespondentFullAdmissionStrategy.class,
    RespondentPartAdmissionStrategy.class,
    RespondentCounterClaimStrategy.class,
    SpecRejectRepaymentPlanStrategy.class,
    RoboticsManualOfflineSupport.class
})
class EventHistoryGoldenSnapshotTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final Path GOLDEN_DIR = Path.of("src/test/resources/robotics/golden").toAbsolutePath();
    private static final LocalDate BASE_DATE = LocalDate.of(2020, 8, 1);

    @Autowired
    private EventHistoryMapper mapper;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private LocationRefDataUtil locationRefDataUtil;

    @MockBean
    private Time time;

    @BeforeEach
    void setup() throws Exception {
        LocalDateTime fixed = LocalDateTime.of(2020, 8, 1, 12, 0);
        when(time.now()).thenReturn(fixed);
        lenient().when(featureToggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);
        lenient().when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
        lenient().when(featureToggleService.isLrAdmissionBulkEnabled()).thenReturn(false);
        lenient().when(featureToggleService.isJOLiveFeedActive()).thenReturn(true);
        lenient().when(locationRefDataUtil.getPreferredCourtData(any(), any(), anyBoolean())).thenReturn("121");

        Files.createDirectories(GOLDEN_DIR);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        objectMapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);
    }

    @Test
    void eventHistoriesMatchGoldenSnapshots() throws Exception {
        for (Map.Entry<String, CaseData> entry : buildScenarios().entrySet()) {
            CaseData caseData = entry.getValue();
            var eventHistory = mapper.buildEvents(caseData, BEARER_TOKEN);

            JsonNode actualNode = objectMapper.valueToTree(eventHistory);
            String resourcePath = "robotics/golden/" + entry.getKey() + ".json";
            var resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
            Path goldenPath = resolveGoldenPath(entry.getKey() + ".json");

            if (resource == null && !Files.exists(goldenPath)) {
                String actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventHistory);
                System.out.println("=== Missing golden snapshot for scenario: " + entry.getKey() + " ===");
                System.out.println(actualJson);
                fail("Golden snapshot not found for scenario '" + entry.getKey() + "'. Create "
                    + GOLDEN_DIR.resolve(entry.getKey() + ".json") + " with the printed payload.");
            }

            Path loadPath = resource != null ? Path.of(resource.toURI()) : goldenPath;
            JsonNode expectedNode = objectMapper.readTree(Files.readString(loadPath));
            String actualJson = objectMapper.writeValueAsString(actualNode);
            String expectedJson = objectMapper.writeValueAsString(expectedNode);
            assertThat(actualJson).isEqualTo(expectedJson);
        }
    }

    private Path resolveGoldenPath(String fileName) {
        Path cwd = Path.of("").toAbsolutePath();
        for (int i = 0; i < 5 && cwd != null; i++) {
            Path candidate = cwd.resolve("src/test/resources/robotics/golden").resolve(fileName);
            if (Files.exists(candidate)) {
                return candidate;
            }
            cwd = cwd.getParent();
        }
        return GOLDEN_DIR.resolve(fileName);
    }

    private Map<String, CaseData> buildScenarios() {
        Map<String, CaseData> scenarios = new LinkedHashMap<>();

        scenarios.put(
            "unspec_claim_progression",
            normalise(CaseDataBuilder.builder().atStateClaimDetailsNotified().build())
        );

        scenarios.put(
            "unspec_taken_offline",
            normalise(CaseDataBuilder.builder().atStateTakenOfflineByStaff().build())
        );

        scenarios.put(
            "unspec_ga_strike_out",
            normalise(
                CaseDataBuilder.builder()
                    .atStateTakenOfflineByStaff()
                    .getGeneralApplicationWithStrikeOut("001")
                    .getGeneralStrikeOutApplicationsDetailsWithCaseState(
                        uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.PROCEEDS_IN_HERITAGE.getDisplayedValue()
                    )
                    .build()
            )
        );

        scenarios.put(
            "unspec_multi_party_same_solicitor",
            normalise(
                CaseDataBuilder.builder()
                    .atStateNotificationAcknowledged1v2SameSolicitor()
                    .multiPartyClaimOneDefendantSolicitor()
                    .build()
            )
        );

        scenarios.put(
            "multi_applicant_proceed",
            normalise(
                CaseDataBuilder.builder()
                    .multiPartyClaimTwoApplicants()
                    .atStateApplicant2RespondToDefenceAndProceed_2v1()
                    .build()
            )
        );

        scenarios.put(
            "spec_full_defence",
            normalise(
                CaseDataBuilder.builder()
                    .setClaimTypeToSpecClaim()
                    .atStateSpec1v1ClaimSubmitted()
                    .atStateRespondent1v1FullDefenceSpec()
                    .build()
            )
        );

        scenarios.put(
            "breathing_space_standard",
            normalise(
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .addLiftBreathingSpace()
                    .build()
            )
        );

        scenarios.put(
            "breathing_space_mental_health",
            normalise(
                CaseDataBuilder.builder()
                    .atStateClaimDetailsNotified()
                    .addLiftMentalBreathingSpace()
                    .build()
            )
        );

        scenarios.put(
            "spec_mediation_part_admit",
            ensureFutureApplicantResponse(normalise(buildSpecMediationPartAdmitCase()))
        );

        scenarios.put(
            "default_judgment_unspec",
            normalise(buildDefaultJudgmentUnspecCase())
        );

        return scenarios;
    }

    private CaseData buildSpecMediationPartAdmitCase() {
        DynamicList locationValues = DynamicList.fromList(List.of("Value 1"));
        DynamicList preferredCourt = DynamicList.builder()
            .listItems(locationValues.getListItems())
            .value(locationValues.getListItems().get(0))
            .build();

        return CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateSpec1v1ClaimSubmitted()
            .atStateRespondent1v1FullAdmissionSpec()
            .build()
            .toBuilder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .responseClaimMediationSpecRequired(YesOrNo.YES)
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1ClaimMediationSpecRequiredLip(
                    ClaimantMediationLip.builder()
                        .hasAgreedFreeMediation(MediationDecision.Yes)
                        .build()
                )
                .build()
            )
            .addRespondent2(YesOrNo.NO)
            .applicant1DQ(
                Applicant1DQ.builder()
                    .applicant1DQRequestedCourt(
                        RequestedCourt.builder()
                            .responseCourtLocations(preferredCourt)
                            .reasonForHearingAtSpecificCourt("test")
                            .build()
                    )
                    .build()
            )
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
            .specDefenceAdmittedRequired(YesOrNo.NO)
            .build();
    }

    private CaseData buildDefaultJudgmentUnspecCase() {
        LocalDateTime djCreated = BASE_DATE.plusDays(9).atTime(10, 0);
        LocalDate paymentDate = BASE_DATE.plusDays(14);
        CaseDataLiP lipResponse = CaseDataLiP.builder()
            .applicant1LiPResponse(
                ClaimantLiPResponse.builder()
                    .claimantCourtDecision(RepaymentDecisionType.IN_FAVOUR_OF_CLAIMANT)
                    .claimantResponseOnCourtDecision(ClaimantResponseOnCourtDecisionType.ACCEPT_REPAYMENT_DATE)
                    .build()
            )
            .build();

        return CaseDataBuilder.builder()
            .getDefaultJudgment1v1Case()
            .toBuilder()
            .joDJCreatedDate(djCreated)
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec(paymentDate)
            .caseDataLiP(lipResponse)
            .totalClaimAmount(BigDecimal.valueOf(1010).setScale(2))
            .build();
    }

    private CaseData normalise(CaseData caseData) {
        return CaseDataNormalizer.normalise(caseData, BASE_DATE);
    }

    private CaseData ensureFutureApplicantResponse(CaseData caseData) {
        LocalDateTime future = BASE_DATE.plusYears(150).atTime(12, 0);
        return caseData.toBuilder()
            .applicant1ResponseDate(future)
            .applicant1ResponseDeadline(future.plusDays(7))
            .build();
    }
}
