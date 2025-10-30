package uk.gov.hmcts.reform.civil.service.flowstate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StateFlowDTO;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_ISSUED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_SUBMITTED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DIVERGENT_RESPOND_GO_OFFLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.IN_MEDIATION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.MEDIATION_UNSUCCESSFUL_PROCEED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.SPEC_DRAFT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_SDO;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT;

@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    SimpleStateFlowEngine.class,
    SimpleStateFlowBuilder.class,
    TransitionsTestConfiguration.class}
)
//CHECKSTYLE:OFF
class ScenarioDrivenFlowStateTest {

    @Autowired
    private SimpleStateFlowEngine stateFlowEngine;

    @SuppressWarnings("unused")
    @MockBean
    private FeatureToggleService featureToggleService;

    @Nested
    class ScenarioDrivenFlowStateEvalNonSpec1v1 {
        /**
        * One claimant vs one defendant. Default
        */
        static Stream<Arguments> milestones_NonSpec_1v1() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(UNSPEC_CLAIM, ONE_V_ONE), DRAFT),
                Arguments.of("Claim Issued", buildCaseDataClaimIssued(UNSPEC_CLAIM, ONE_V_ONE), CLAIM_ISSUED),
                Arguments.of("Claim Submitted", buildCaseDataClaimSubmitted(UNSPEC_CLAIM, ONE_V_ONE), CLAIM_SUBMITTED),
                Arguments.of(
                    "Claim Notified from issued",
                    buildCaseDataClaimNotified(UNSPEC_CLAIM, ONE_V_ONE),
                    CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified from Notified",
                    buildCaseDataClaimDetailsNotified(UNSPEC_CLAIM, ONE_V_ONE),
                    CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified TimeExtended from Notified",
                    buildCaseDataClaimDetailsNotifiedTE(UNSPEC_CLAIM, ONE_V_ONE),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION
                ),
                Arguments.of(
                    "Claim Acknowledged",
                    buildCaseDataNotificationAcknowledged(UNSPEC_CLAIM, ONE_V_ONE),
                    NOTIFICATION_ACKNOWLEDGED
                ),
                Arguments.of(
                    "Claim Acknowledged Time Extension",
                    buildCaseDataNotificationAcknowledgedTE(UNSPEC_CLAIM, ONE_V_ONE),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(UNSPEC_CLAIM, ONE_V_ONE),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(UNSPEC_CLAIM, ONE_V_ONE),
                    PART_ADMISSION
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(UNSPEC_CLAIM, ONE_V_ONE),
                    FULL_ADMISSION
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(UNSPEC_CLAIM, ONE_V_ONE),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(UNSPEC_CLAIM, ONE_V_ONE),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Mediation branch in mediation",
                    buildCaseDataInMediation(UNSPEC_CLAIM, ONE_V_ONE),
                    IN_MEDIATION
                ),
                Arguments.of(
                    "Mediation branch: unsuccessful - proceed",
                    buildCaseDataMediationUnsuccessfulProceed(UNSPEC_CLAIM, ONE_V_ONE),
                    MEDIATION_UNSUCCESSFUL_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(UNSPEC_CLAIM, ONE_V_ONE),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Claim Details Notified",
                    buildCaseDataTakenOfflineAfterClaimDetailsNotified(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(UNSPEC_CLAIM, ONE_V_ONE),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(UNSPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_BY_STAFF
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_NonSpec_1v1")
        void shouldDeriveExpectedState_1v1(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlow(caseData);
            assertThat(dto.getState().getName())
                .as("1v1 Non-Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalNonSpec1v21LR {
        /**
         * one claimant vs two defendants with one LR for both defendants.
         */
        static Stream<Arguments> milestones_NonSpec_1v2_1LR() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP), DRAFT),
                Arguments.of(
                    "Claim Issued",
                    buildCaseDataClaimIssued(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim Submitted",
                    buildCaseDataClaimSubmitted(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_SUBMITTED
                ),
                Arguments.of(
                    "Claim Notified from issued",
                    buildCaseDataClaimNotified(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified from Notified",
                    buildCaseDataClaimDetailsNotified(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified TimeExtended from Notified",
                    buildCaseDataClaimDetailsNotifiedTE(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION
                ),
                Arguments.of(
                    "Claim Acknowledged",
                    buildCaseDataNotificationAcknowledged(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    NOTIFICATION_ACKNOWLEDGED
                ),
                Arguments.of(
                    "Claim Acknowledged Time Extension",
                    buildCaseDataNotificationAcknowledgedTE(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    PART_ADMISSION
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_ADMISSION
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Claim Details Notified",
                    buildCaseDataTakenOfflineAfterClaimDetailsNotified(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_BY_STAFF
                ),
                Arguments.of(
                    "Divergent response",
                    buildCaseDataFirstResponse_Divergent(UNSPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    DIVERGENT_RESPOND_GO_OFFLINE
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_NonSpec_1v2_1LR")
        void shouldDeriveExpectedState_1v2_1LR(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlow(caseData);
            assertThat(dto.getState().getName())
                .as("1v2 1LR Non-Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalNonSpec1v22LR {
        /**
         * one claimant vs two defendants with one LR for each defendant.
         */
        static Stream<Arguments> milestones_NonSpec_1v2_2LR() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP), DRAFT),
                Arguments.of(
                    "Claim Issued",
                    buildCaseDataClaimIssued(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim Submitted",
                    buildCaseDataClaimSubmitted(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_SUBMITTED
                ),
                Arguments.of(
                    "Claim Notified from issued",
                    buildCaseDataClaimNotified(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified from Notified",
                    buildCaseDataClaimDetailsNotified(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified TimeExtended from Notified",
                    buildCaseDataClaimDetailsNotifiedTE(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION
                ),
                Arguments.of(
                    "Claim Acknowledged",
                    buildCaseDataNotificationAcknowledged(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    NOTIFICATION_ACKNOWLEDGED
                ),
                Arguments.of(
                    "Claim Acknowledged Time Extension",
                    buildCaseDataNotificationAcknowledgedTE(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    PART_ADMISSION
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_ADMISSION
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Claim Details Notified",
                    buildCaseDataTakenOfflineAfterClaimDetailsNotified(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_BY_STAFF
                ),
                Arguments.of(
                    "Divergent response",
                    buildCaseDataFirstResponse_Divergent(UNSPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    DIVERGENT_RESPOND_GO_OFFLINE
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_NonSpec_1v2_2LR")
        void shouldDeriveExpectedState_1v2_2LR(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlow(caseData);
            assertThat(dto.getState().getName())
                .as("1v2 2LR Non-Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalNonSpec2v1 {
        /**
         * two claimants vs one defendant.
         */
        static Stream<Arguments> milestones_NonSpec_2v1() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(UNSPEC_CLAIM, TWO_V_ONE), DRAFT),
                Arguments.of("Claim Issued", buildCaseDataClaimIssued(UNSPEC_CLAIM, TWO_V_ONE), CLAIM_ISSUED),
                Arguments.of("Claim Submitted", buildCaseDataClaimSubmitted(UNSPEC_CLAIM, TWO_V_ONE), CLAIM_SUBMITTED),
                Arguments.of(
                    "Claim Notified from issued",
                    buildCaseDataClaimNotified(UNSPEC_CLAIM, TWO_V_ONE),
                    CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified from Notified",
                    buildCaseDataClaimDetailsNotified(UNSPEC_CLAIM, TWO_V_ONE),
                    CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Claim DetailsNotified TimeExtended from Notified",
                    buildCaseDataClaimDetailsNotifiedTE(UNSPEC_CLAIM, TWO_V_ONE),
                    CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION
                ),
                Arguments.of(
                    "Claim Acknowledged",
                    buildCaseDataNotificationAcknowledged(UNSPEC_CLAIM, TWO_V_ONE),
                    NOTIFICATION_ACKNOWLEDGED
                ),
                Arguments.of(
                    "Claim Acknowledged Time Extension",
                    buildCaseDataNotificationAcknowledgedTE(UNSPEC_CLAIM, TWO_V_ONE),
                    NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(UNSPEC_CLAIM, TWO_V_ONE),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(UNSPEC_CLAIM, TWO_V_ONE),
                    DIVERGENT_RESPOND_GO_OFFLINE
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(UNSPEC_CLAIM, TWO_V_ONE),
                    DIVERGENT_RESPOND_GO_OFFLINE
                    // FULL_ADMISSION - 2v1, Non‑SPEC, first response Full Admission -> always offline for non‑full‑defence
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(UNSPEC_CLAIM, TWO_V_ONE),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(UNSPEC_CLAIM, TWO_V_ONE),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Mediation branch in mediation",
                    buildCaseDataInMediation(UNSPEC_CLAIM, TWO_V_ONE),
                    IN_MEDIATION
                ),
                Arguments.of(
                    "Mediation branch: unsuccessful - proceed",
                    buildCaseDataMediationUnsuccessfulProceed(UNSPEC_CLAIM, TWO_V_ONE),
                    MEDIATION_UNSUCCESSFUL_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(UNSPEC_CLAIM, TWO_V_ONE),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Claim Details Notified",
                    buildCaseDataTakenOfflineAfterClaimDetailsNotified(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(UNSPEC_CLAIM, TWO_V_ONE),
                    PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(UNSPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_BY_STAFF
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_NonSpec_2v1")
        void shouldDeriveExpectedState_2v1(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlow(caseData);
            assertThat(dto.getState().getName())
                .as("2v1 Non-Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalSpec1v1 {

        static Stream<Arguments> milestones_Spec_1v1() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(SPEC_CLAIM, ONE_V_ONE), SPEC_DRAFT),
                Arguments.of("Claim Issued", buildCaseDataClaimIssued(SPEC_CLAIM, ONE_V_ONE), CLAIM_ISSUED),
                Arguments.of("Claim Submitted", buildCaseDataClaimSubmitted(SPEC_CLAIM, ONE_V_ONE), CLAIM_SUBMITTED),
                Arguments.of(
                    "Claim Notified does not transition for SPEC_CLAIM",
                    buildCaseDataClaimNotified(SPEC_CLAIM, ONE_V_ONE),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim DetailsNotified does not transition for SPEC_CLAIM",
                    buildCaseDataClaimDetailsNotified(SPEC_CLAIM, TWO_V_ONE),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim DetailsNotified TE does not transition for SPEC_CLAIM",
                    buildCaseDataClaimDetailsNotifiedTE(SPEC_CLAIM, TWO_V_ONE),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim Acknowledged does not transition for SPEC_CLAIM",
                    buildCaseDataNotificationAcknowledged(SPEC_CLAIM, TWO_V_ONE),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim Acknowledged TE does not transition for SPEC_CLAIM",
                    buildCaseDataNotificationAcknowledgedTE(SPEC_CLAIM, TWO_V_ONE),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Taken offline Modify Selected Code",
                    buildCaseDataTakenOfflineAfterClaimDetailsNotified(SPEC_CLAIM, TWO_V_ONE),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(SPEC_CLAIM, ONE_V_ONE),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(SPEC_CLAIM, ONE_V_ONE),
                    PART_ADMISSION
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(SPEC_CLAIM, ONE_V_ONE),
                    FULL_ADMISSION
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(SPEC_CLAIM, ONE_V_ONE),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(SPEC_CLAIM, ONE_V_ONE),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Mediation branch in mediation",
                    buildCaseDataInMediation(SPEC_CLAIM, ONE_V_ONE),
                    IN_MEDIATION
                ),
                Arguments.of(
                    "Mediation branch: unsuccessful - proceed",
                    buildCaseDataMediationUnsuccessfulProceed(SPEC_CLAIM, ONE_V_ONE),
                    MEDIATION_UNSUCCESSFUL_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(SPEC_CLAIM, ONE_V_ONE),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(SPEC_CLAIM, ONE_V_ONE),
                    TAKEN_OFFLINE_BY_STAFF
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_Spec_1v1")
        void shouldDeriveExpectedState_1v1(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlowSpec(caseData);
            assertThat(dto.getState().getName())
                .as("1v1 Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalSpec1v21LR {
        /**
         * one claimant vs two defendants with one LR for both defendants.
         */
        static Stream<Arguments> milestones_Spec_1v2_1LR() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP), SPEC_DRAFT),
                Arguments.of(
                    "Claim Issued",
                    buildCaseDataClaimIssued(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim Submitted",
                    buildCaseDataClaimSubmitted(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_SUBMITTED
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    PART_ADMISSION
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_ADMISSION
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    TAKEN_OFFLINE_BY_STAFF
                ),
                Arguments.of(
                    "Divergent response",
                    buildCaseDataFirstResponse_Divergent(SPEC_CLAIM, ONE_V_TWO_ONE_LEGAL_REP),
                    DIVERGENT_RESPOND_GO_OFFLINE
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_Spec_1v2_1LR")
        void shouldDeriveExpectedState_1v2_1LR(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlowSpec(caseData);
            assertThat(dto.getState().getName())
                .as("1v2 1LR Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalSpec1v22LR {
        /**
         * one claimant vs two defendants with one LR for each defendant.
         */
        static Stream<Arguments> milestones_Spec_1v2_2LR() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP), SPEC_DRAFT),
                Arguments.of(
                    "Claim Issued",
                    buildCaseDataClaimIssued(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_ISSUED
                ),
                Arguments.of(
                    "Claim Submitted",
                    buildCaseDataClaimSubmitted(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_SUBMITTED
                ),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    TAKEN_OFFLINE_BY_STAFF
                ),
                Arguments.of(
                    "Divergent response",
                    buildCaseDataFirstResponse_Divergent(SPEC_CLAIM, ONE_V_TWO_TWO_LEGAL_REP),
                    DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_Spec_1v2_2LR")
        void shouldDeriveExpectedState_1v2_2LR(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlowSpec(caseData);
            assertThat(dto.getState().getName())
                .as("1v2 2LR Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    class ScenarioDrivenFlowStateEvalSpec2v1 {
        /**
         * two claimants vs one defendant.
         */
        static Stream<Arguments> milestones_Spec_2v1() {
            return Stream.of(
                Arguments.of("Draft", buildCaseDataDraft(SPEC_CLAIM, TWO_V_ONE), SPEC_DRAFT),
                Arguments.of("Claim Issued", buildCaseDataClaimIssued(SPEC_CLAIM, TWO_V_ONE), CLAIM_ISSUED),
                Arguments.of("Claim Submitted", buildCaseDataClaimSubmitted(SPEC_CLAIM, TWO_V_ONE), CLAIM_SUBMITTED),
                Arguments.of(
                    "First response Full Defence",
                    buildCaseDataFirstResponse_FullDefence(SPEC_CLAIM, TWO_V_ONE),
                    FULL_DEFENCE
                ),
                Arguments.of(
                    "First response Part Admission",
                    buildCaseDataFirstResponse_PartAdmission(SPEC_CLAIM, TWO_V_ONE),
                    PART_ADMISSION
                ),
                Arguments.of(
                    "First response Full Admission",
                    buildCaseDataFirstResponse_FullAdmission(SPEC_CLAIM, TWO_V_ONE),
                    FULL_ADMISSION
                ),
                Arguments.of(
                    "Applicant response Full Defence proceed",
                    buildCaseDataFullDefenceProceed(SPEC_CLAIM, TWO_V_ONE),
                    FULL_DEFENCE_PROCEED
                ),
                Arguments.of(
                    "Applicant response Full Defence NOT proceed",
                    buildCaseDataFullDefenceNotProceed(SPEC_CLAIM, TWO_V_ONE),
                    FULL_DEFENCE_NOT_PROCEED
                ),
                Arguments.of(
                    "Mediation branch in mediation",
                    buildCaseDataInMediation(SPEC_CLAIM, TWO_V_ONE),
                    IN_MEDIATION
                ),
                Arguments.of(
                    "Mediation branch: unsuccessful - proceed",
                    buildCaseDataMediationUnsuccessfulProceed(SPEC_CLAIM, TWO_V_ONE),
                    MEDIATION_UNSUCCESSFUL_PROCEED
                ),
                Arguments.of(
                    "Default judgment eligibility",
                    buildCaseDataDJPastHearingFeeDueDeadline(SPEC_CLAIM, TWO_V_ONE),
                    CLAIM_DISMISSED_HEARING_FEE_DUE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline After Claim Notified",
                    buildCaseDataTakenOfflineAfterClaimNotified(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED
                ),
                Arguments.of(
                    "Taken offline After Sdo",
                    buildCaseDataTakenOfflineAfterSdo(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_AFTER_SDO
                ),
                Arguments.of(
                    "Taken offline Sdo Not Drawn",
                    buildCaseDataTakenOfflineSdoNotDrawn(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_SDO_NOT_DRAWN
                ),
                Arguments.of(
                    "Taken offline Unrepresented",
                    buildCaseDataTakenOfflineUnrepresented(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unregistered",
                    buildCaseDataTakenOfflineUnregistered(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Unrepresented Unregistered",
                    buildCaseDataTakenOfflineUnrepresentedUnregistered(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT
                ),
                Arguments.of(
                    "Taken offline Past Applicant Response Deadline",
                    buildCaseDataTakenOfflinePastApplicantResponseDeadline(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE
                ),
                Arguments.of(
                    "Taken offline By Staff",
                    buildCaseDataTakenOfflineByStaff(SPEC_CLAIM, TWO_V_ONE),
                    TAKEN_OFFLINE_BY_STAFF
                )
            );
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("milestones_Spec_2v1")
        void shouldDeriveExpectedState_2v1(String description, CaseData caseData, FlowState.Main expected) {
            StateFlowDTO dto = stateFlowEngine.getStateFlowSpec(caseData);
            assertThat(dto.getState().getName())
                .as("2v1 Spec scenario: %s", description)
                .isEqualTo(expected.fullName());
            assertThat(dto.getStateHistory()).isNotNull().isNotEmpty();
        }
    }

    private static CaseData buildCaseDataDraft(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atStateClaimDraft().build();
    }

    private static CaseData buildCaseDataClaimSubmitted(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(CLAIM_SUBMITTED).build();
    }

    private static CaseData buildCaseDataClaimIssued(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(CLAIM_ISSUED).build();
    }

    private static CaseData buildCaseDataClaimNotified(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(CLAIM_NOTIFIED).build();
    }

    private static CaseData buildCaseDataClaimDetailsNotified(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(CLAIM_DETAILS_NOTIFIED).build();
    }

    private static CaseData buildCaseDataClaimDetailsNotifiedTE(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION).build();
    }

    private static CaseData buildCaseDataNotificationAcknowledged(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(NOTIFICATION_ACKNOWLEDGED).build();
    }

    private static CaseData buildCaseDataNotificationAcknowledgedTE(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION).build();
    }

    private static CaseData buildCaseDataFirstResponse_PartAdmission(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentPartAdmission()
                        .respondent2Responds(RespondentResponseType.PART_ADMISSION)
                        .respondentResponseIsSame(YES);
                case TWO_V_ONE -> builder.atStateRespondentPartAdmission()
                    .respondent1ClaimResponseTypeToApplicant2(
                    RespondentResponseType.PART_ADMISSION);
                default -> builder.atStateRespondentPartAdmission();
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentPartAdmissionSpec()
                        .respondent1ClaimResponseTypeForSpec(
                        RespondentResponseTypeSpec.PART_ADMISSION)
                        .respondent2RespondsSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                        .respondentResponseIsSame(YES);
                case TWO_V_ONE ->
                    builder.atStateRespondentPartAdmissionSpec()
                        .atStateRespondent2v1PartAdmission(); //SPEC Only
                default -> builder.atStateRespondentPartAdmissionSpec();
            };
        };

        return builder.build();
    }

    private static CaseData buildCaseDataFirstResponse_FullAdmission(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullAdmission()
                        .respondent2Responds(RespondentResponseType.FULL_ADMISSION)
                        .respondentResponseIsSame(YES);
                case TWO_V_ONE -> builder.atStateRespondentFullAdmission()
                    .respondent1ClaimResponseTypeToApplicant2(
                    RespondentResponseType.FULL_ADMISSION);
                default -> builder.atStateRespondentFullAdmission();
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullAdmissionSpec()
                        .respondent2RespondsSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                        .respondentResponseIsSame(YES);
                case TWO_V_ONE ->
                    builder.atStateRespondentFullAdmissionSpec()
                        .atStateRespondent2v1FullAdmission(); //SPEC Only
                default -> builder.atStateRespondentFullAdmissionSpec();
            };
        };

        return builder.build();
    }

    private static CaseData buildCaseDataFirstResponse_FullDefence(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                        .respondentResponseIsSame(YES);
                case TWO_V_ONE -> builder.atStateRespondentFullDefence()
                    .respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_DEFENCE);
                default -> builder.atStateRespondentFullDefence();
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .respondentResponseIsSame(YES);
                case TWO_V_ONE ->
                    builder.atStateRespondentFullDefenceSpec().atStateRespondent2v1FullDefence(); //SPEC Only
                default -> builder.atStateRespondentFullDefenceSpec();
            };
        };

        return builder.build();
    }

    private static CaseData buildCaseDataFirstResponse_Divergent(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP ->
                    builder.atState1v2SameSolicitorDivergentResponse(
                        RespondentResponseType.PART_ADMISSION,
                        RespondentResponseType.FULL_ADMISSION
                    );
                case ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atState1v2DivergentResponse(
                        RespondentResponseType.FULL_DEFENCE,
                        RespondentResponseType.PART_ADMISSION
                    );
                default -> builder;
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP ->
                    builder.atState1v2SameSolicitorDivergentResponseSpec(
                        RespondentResponseTypeSpec.PART_ADMISSION,
                        RespondentResponseTypeSpec.FULL_ADMISSION
                    );
                case ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atState1v2DifferentSolicitorDivergentResponseSpec(
                        RespondentResponseTypeSpec.PART_ADMISSION,
                        RespondentResponseTypeSpec.FULL_ADMISSION
                    );
                default -> builder;
            };
        };

        return builder.build();
    }

    private static CaseData buildCaseDataFullDefenceProceed(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                        .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES);
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_DEFENCE)
                        .applicant1ProceedWithClaimMultiParty2v1(YES)
                        .applicant2ProceedWithClaimMultiParty2v1(YES);
                default -> builder.applicant1ProceedWithClaim(YES);
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .applicant1ProceedWithClaim(YES);
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .defendantSingleResponseToBothClaimants(YES)
                        .applicant1ProceedWithClaimSpec2v1(YES);
                default -> builder.atStateRespondent1v1FullDefenceSpec()
                    .applicant1ProceedWithClaim(YES);
            };
        };

        return builder.atStateApplicantRespondToDefenceAndProceed(partyShape).build();
    }

    private static CaseData buildCaseDataFullDefenceNotProceed(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(NO)
                        .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(NO);
                case TWO_V_ONE ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_DEFENCE)
                        .applicant1ProceedWithClaimMultiParty2v1(NO)
                        .applicant2ProceedWithClaimMultiParty2v1(NO);
                default ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .applicant1ProceedWithClaim(NO);
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .applicant1ProceedWithClaim(NO);
                case TWO_V_ONE ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .defendantSingleResponseToBothClaimants(YES)
                        .applicant1ProceedWithClaimSpec2v1(NO);
                default ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .atStateRespondent1v1FullDefenceSpec()
                        .applicant1ProceedWithClaim(NO);
            };
        };

        return builder.build();
    }

    private static CaseData buildCaseDataInMediation(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (partyShape) {
            case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                builder.addApplicant1MediationInfo().addApplicant1MediationAvailability()
                    .addRespondent1MediationInfo().addRespondent1MediationAvailability()
                    .addRespondent2MediationInfo().addRespondent2MediationAvailability();
            default ->
                builder.addApplicant1MediationInfo().addApplicant1MediationAvailability()
                    .addRespondent1MediationInfo().addRespondent1MediationAvailability();
        };

        return builder.atStateApplicantProceedAllMediation(partyShape).build();
    }

    private static CaseData buildCaseDataMediationUnsuccessfulProceed(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atStateMediationUnsuccessful(partyShape)
            .respondent1ResponseDate(LocalDateTime.now()).build();
    }

    private static CaseData buildCaseDataDJPastHearingFeeDueDeadline(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case TWO_V_ONE ->
                    builder.atStateApplicant1RespondToDefenceAndProceed_2v1()
                        .caseDismissedHearingFeeDueDate(
                        LocalDateTime.now().minusDays(1));
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2()
                        .caseDismissedHearingFeeDueDate(
                        LocalDateTime.now().minusDays(1));
                default ->
                    builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
                        .caseDismissedHearingFeeDueDate(LocalDateTime.now().minusDays(1));
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case TWO_V_ONE ->
                    builder.atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC()
                        .caseDismissedHearingFeeDueDate(LocalDateTime.now().minusDays(1));
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .applicant1ProceedWithClaim(YES)
                        .caseDismissedHearingFeeDueDate(LocalDateTime.now().minusDays(1));
                default ->
                    builder.atStateRespondent1v1FullDefenceSpec()
                        .applicant1ProceedWithClaim(YES)
                        .caseDismissedHearingFeeDueDate(LocalDateTime.now().minusDays(1));
            };
        };

        return builder.atStateClaimDismissedPastHearingFeeDueDeadline(partyShape).build();
    }

    private static CaseData buildCaseDataTakenOfflineAfterClaimNotified(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        return builder.atState(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED).build();
    }

    private static CaseData buildCaseDataTakenOfflineAfterClaimDetailsNotified(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED).build();
    }

    private static CaseData buildCaseDataTakenOfflineSdoNotDrawn(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                        .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES);
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_DEFENCE)
                        .applicant1ProceedWithClaimMultiParty2v1(YES)
                        .applicant2ProceedWithClaimMultiParty2v1(YES);
                default -> builder.applicant1ProceedWithClaim(YES);
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .applicant1ProceedWithClaim(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .defendantSingleResponseToBothClaimants(
                        YES).applicant1ProceedWithClaimSpec2v1(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
                default ->
                    builder.atStateRespondent1v1FullDefenceSpec()
                        .applicant1ProceedWithClaim(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(
                        LocalDateTime.now().plusDays(14));
            };
        };

        return builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
            .atStateTakenOfflineSDONotDrawn(partyShape)
            .build();
    }

    private static CaseData buildCaseDataTakenOfflineAfterSdo(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                        .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES);
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_DEFENCE)
                        .applicant1ProceedWithClaimMultiParty2v1(YES)
                        .applicant2ProceedWithClaimMultiParty2v1(YES);
                default -> builder.applicant1ProceedWithClaim(YES);
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .applicant1ProceedWithClaim(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(
                        LocalDateTime.now().plusDays(14));
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .defendantSingleResponseToBothClaimants(
                        YES).applicant1ProceedWithClaimSpec2v1(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
                default ->
                    builder.atStateRespondent1v1FullDefenceSpec()
                        .applicant1ProceedWithClaim(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
            };
        };

        return builder.atStateApplicantRespondToDefenceAndProceed(partyShape)
            .atStateTakenOfflineAfterSDO(partyShape)
            .build();
    }

    private static CaseData buildCaseDataTakenOfflineByStaff(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> builder.atStateTakenOfflineByStaff();
            case SPEC_CLAIM ->
                builder.atStateTakenOfflineByStaffSpec().setClaimNotificationDate()
                    .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
        };

        return builder.build();
    }

    private static CaseData buildCaseDataTakenOfflinePastApplicantResponseDeadline(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);

        builder = switch (category) {
            case UNSPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
                        .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YES);
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_DEFENCE)
                        .applicant1ProceedWithClaimMultiParty2v1(YES)
                        .applicant2ProceedWithClaimMultiParty2v1(YES);
                default -> builder.applicant1ProceedWithClaim(YES);
            };
            case SPEC_CLAIM -> switch (partyShape) {
                case ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    builder.atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses()
                        .applicant1ProceedWithClaim(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
                case TWO_V_ONE ->
                    builder.respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                        .defendantSingleResponseToBothClaimants(
                        YES).applicant1ProceedWithClaimSpec2v1(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
                default ->
                    builder.atStateRespondent1v1FullDefenceSpec().applicant1ProceedWithClaim(YES)
                        .atStatePendingClaimIssued()
                        .setClaimNotificationDate()
                        .claimNotificationDeadline(LocalDateTime.now().plusDays(14));
            };
        };

        return builder.atStateApplicantRespondToDefenceAndProceed(partyShape).atStateTakenOfflinePastApplicantResponseDeadline().build();
    }

    private static CaseData buildCaseDataTakenOfflineUnregistered(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(TAKEN_OFFLINE_UNREGISTERED_DEFENDANT).build();
    }

    private static CaseData buildCaseDataTakenOfflineUnrepresented(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT).takenOfflineDate(LocalDateTime.now()).build();
    }

    private static CaseData buildCaseDataTakenOfflineUnrepresentedUnregistered(CaseCategory category, MultiPartyScenario partyShape) {
        CaseDataBuilder builder = applyCategoryAndShape(CaseDataBuilder.builder(), category, partyShape);
        return builder.atState(TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT).build();
    }

    private static CaseDataBuilder applyCategoryAndShape(CaseDataBuilder builder, CaseCategory category, MultiPartyScenario shape) {
        builder.caseAccessCategory(category);

        if (category == SPEC_CLAIM) {
            // Ensure Spec flows have minimal non-null LiP structure to avoid NPEs in predicates
            builder.caseDataLip(CaseDataLiP.builder().applicant1SettleClaim(NO).build());
        }

        return switch (shape) {
            case ONE_V_TWO_ONE_LEGAL_REP -> {
                if (category == SPEC_CLAIM) {
                    yield builder
                        .multiPartyClaimTwoDefendantSameSolicitorsSpec()
                        .respondent2SameLegalRepresentative(YES)
                        .respondent2Represented(YES);
                }
                yield builder
                    .multiPartyClaimOneDefendantSolicitor()
                    .respondent2SameLegalRepresentative(YES)
                    .respondent2Represented(YES);
            }
            case ONE_V_TWO_TWO_LEGAL_REP -> {
                if (category == SPEC_CLAIM) {
                    yield builder
                        .multiPartyClaimTwoDefendantSolicitorsSpec()
                        .respondent2SameLegalRepresentative(NO);
                }
                yield builder
                    .multiPartyClaimTwoDefendantSolicitors()
                    .respondent2SameLegalRepresentative(NO);
            }
            case TWO_V_ONE -> builder.multiPartyClaimTwoApplicants();
            default -> builder;
        };
    }
}
