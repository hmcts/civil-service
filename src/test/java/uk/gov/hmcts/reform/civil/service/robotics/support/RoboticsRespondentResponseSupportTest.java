package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoboticsRespondentResponseSupportTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2024, 1, 10, 12, 30);

    private final RoboticsEventTextFormatter formatter = new RoboticsEventTextFormatter();
    private final RoboticsTimelineHelper timelineHelper = new RoboticsTimelineHelper(() -> NOW);
    private final RoboticsRespondentResponseSupport support = new RoboticsRespondentResponseSupport(formatter, timelineHelper);

    @Test
    void resolveRespondent2ResponseDatePrefersActualTimestamp() {
        LocalDateTime respondent1Date = NOW.minusDays(1);
        LocalDateTime respondent2Date = NOW.plusDays(1);

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.FULL_DEFENCE)
            .respondent1ResponseDate(respondent1Date)
            .respondent2ResponseDate(respondent2Date)
            .build();

        assertThat(support.resolveRespondent2ResponseDate(caseData)).isEqualTo(respondent2Date);
    }

    @Test
    void resolveRespondent2ResponseDateFallsBackToRespondent1WhenTimestampMissing() {
        LocalDateTime respondent1Date = NOW.minusDays(2);

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.FULL_DEFENCE)
            .respondent1ResponseDate(respondent1Date)
            .respondent2ResponseDate(null)
            .build();

        assertThat(support.resolveRespondent2ResponseDate(caseData)).isEqualTo(respondent1Date);
    }

    @Test
    void prepareRespondentResponseTextFallsBackWhenResponseTypeMissing() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicant2RespondToDefenceAndProceed_2v1()
            .build()
            .toBuilder()
            .respondent1ClaimResponseType(null)
            .respondent1ClaimResponseTypeForSpec(null)
            .build();

        var respondent1 = caseData.getRespondent1();
        assertThatThrownBy(() -> support.prepareRespondentResponseText(caseData, respondent1, true))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void prepareRespondentResponseTextKeepsPaginationForCounterClaims() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.COUNTER_CLAIM)
            .build();

        String respondent1Message = support.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
        String respondent2Message = support.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

        assertThat(respondent1Message)
            .contains("[1 of 2 - 2024-01-10]")
            .contains("COUNTER_CLAIM");
        assertThat(respondent2Message)
            .contains("[2 of 2 - 2024-01-10]")
            .contains("COUNTER_CLAIM");
    }

    @Test
    void prepareRespondentResponseTextUsesSpecLabelsForSingleParty() {
        CaseData caseData = CaseDataBuilder.builder()
            .setClaimTypeToSpecClaim()
            .atStateRespondentFullAdmission()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        String message = support.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);

        assertThat(message).isEqualTo(formatter.defendantFullyAdmits());
    }

    @Test
    void paginationUsesIndexOneWhenSecondResponseMissing() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.PART_ADMISSION)
            .respondent1ResponseDate(NOW.minusDays(1))
            .respondent2ResponseDate(null)
            .build();

        String respondent1Message = support.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
        String respondent2Message = support.prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);

        assertThat(respondent1Message).contains("[1 of 2 - 2024-01-10]");
        assertThat(respondent2Message).contains("[1 of 2 - 2024-01-10]");
    }

    @Test
    void resolveRespondent2ActualOrFallbackDateUsesRespondent1WhenMissing() {
        LocalDateTime respondent1Date = NOW.minusDays(3);
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimOneDefendantSolicitor()
            .atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType.FULL_DEFENCE)
            .respondent1ResponseDate(respondent1Date)
            .respondent2ResponseDate(null)
            .build();

        assertThat(support.resolveRespondent2ActualOrFallbackDate(caseData)).isEqualTo(respondent1Date);
    }

    @Test
    void usesSingleResponseFlagForSpecTwoVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .setClaimTypeToSpecClaim()
            .build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefendantSingleResponseToBothClaimants(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES);

        String message = support.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);

        assertThat(message).isEqualTo(formatter.defendantFullyAdmits());
    }

    @Test
    void usesClaimantResponseWhenSingleResponseIsNoForSpecTwoVOne() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .setClaimTypeToSpecClaim()
            .build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setClaimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        caseData.setDefendantSingleResponseToBothClaimants(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);

        String message = support.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);

        assertThat(message).isEqualTo(formatter.defendantPartialAdmission());
    }

    @Test
    void resolveRespondent2ResponseDateFallsBackWhenTwoSolicitors() {
        LocalDateTime respondent1Date = NOW.minusDays(1);

        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateBothRespondentsSameResponse(RespondentResponseType.FULL_DEFENCE)
            .respondent1ResponseDate(respondent1Date)
            .respondent2ResponseDate(null)
            .build();

        assertThat(support.resolveRespondent2ResponseDate(caseData)).isEqualTo(respondent1Date);
    }

    @Test
    void prepareRespondentResponseTextForTwoSolicitorsIncludesResponseType() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateBothRespondentsSameResponse(RespondentResponseType.FULL_DEFENCE)
            .build();

        String message = support.prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);

        assertThat(message)
            .contains("Defendant: " + caseData.getRespondent1().getPartyName())
            .contains("FULL_DEFENCE");
    }
}
