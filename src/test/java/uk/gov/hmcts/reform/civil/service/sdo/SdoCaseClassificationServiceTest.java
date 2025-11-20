package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoCaseClassificationServiceTest {

    private final SdoCaseClassificationService service = new SdoCaseClassificationService();

    @Test
    void shouldDetectSmallClaimsTrack() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.smallClaimsTrack)
            .build();

        assertThat(service.isSmallClaimsTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectSmallClaimsTrackWhenOrderRequested() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.YES)
            .build();

        assertThat(service.isSmallClaimsTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectFastTrack() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
            .build();

        assertThat(service.isFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectFastTrackWhenOrderTypeApplies() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaims(YesOrNo.NO)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        assertThat(service.isFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectNihlFastTrack() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .fastClaims(List.of(FastTrack.fastClaimNoiseInducedHearingLoss))
            .build();

        assertThat(service.isNihlFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectNihlFastTrackFromAdditionalDirections() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .trialAdditionalDirectionsForFastTrack(List.of(FastTrack.fastClaimNoiseInducedHearingLoss))
            .build();

        assertThat(service.isNihlFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectDrhSmallClaim() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
            .build();

        assertThat(service.isDrhSmallClaim(caseData)).isTrue();
    }

    @Test
    void shouldDetectDrhSmallClaimFromAdditionalDirections() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .drawDirectionsOrderSmallClaimsAdditionalDirections(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
            .build();

        assertThat(service.isDrhSmallClaim(caseData)).isTrue();
    }

    @Test
    void shouldFallbackToFalseForNonMatchingCase() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.YES)
            .orderType(OrderType.DECIDE_DAMAGES)
            .build();

        assertThat(service.isSmallClaimsTrack(caseData)).isFalse();
        assertThat(service.isFastTrack(caseData)).isFalse();
        assertThat(service.isNihlFastTrack(caseData)).isFalse();
        assertThat(service.isDrhSmallClaim(caseData)).isFalse();
    }

    @Test
    void shouldDetectAdditionalParties() {
        Party applicant2 = Party.builder().type(Party.Type.INDIVIDUAL).build();
        Party respondent2 = Party.builder().type(Party.Type.INDIVIDUAL).build();

        CaseData caseData = CaseData.builder()
            .applicant2(applicant2)
            .respondent2(respondent2)
            .build();

        assertThat(service.hasApplicant2(caseData)).isTrue();
        assertThat(service.hasRespondent2(caseData)).isTrue();
    }

    @Test
    void shouldReportMissingAdditionalParties() {
        CaseData caseData = CaseData.builder().build();

        assertThat(service.hasApplicant2(caseData)).isFalse();
        assertThat(service.hasRespondent2(caseData)).isFalse();
    }
}
