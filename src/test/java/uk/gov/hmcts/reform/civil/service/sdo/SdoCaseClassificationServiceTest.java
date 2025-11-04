package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;

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
    void shouldDetectFastTrack() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .claimsTrack(ClaimsTrack.fastTrack)
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
    void shouldDetectDrhSmallClaim() {
        CaseData caseData = CaseData.builder()
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
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
}

