package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.ClaimsTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderType;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SdoCaseClassificationServiceTest {

    private final SdoCaseClassificationService service = new SdoCaseClassificationService();

    @Test
    void shouldDetectSmallClaimsTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.smallClaimsTrack);

        assertThat(service.isSmallClaimsTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectSmallClaimsTrackWhenOrderRequested() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.YES);

        assertThat(service.isSmallClaimsTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectFastTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setClaimsTrack(ClaimsTrack.fastTrack);

        assertThat(service.isFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectFastTrackWhenOrderTypeApplies() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setDrawDirectionsOrderSmallClaims(YesOrNo.NO);
        caseData.setOrderType(OrderType.DECIDE_DAMAGES);

        assertThat(service.isFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectNihlFastTrack() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setFastClaims(List.of(FastTrack.fastClaimNoiseInducedHearingLoss));

        assertThat(service.isNihlFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectNihlFastTrackFromAdditionalDirections() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setTrialAdditionalDirectionsForFastTrack(
            List.of(FastTrack.fastClaimNoiseInducedHearingLoss)
        );

        assertThat(service.isNihlFastTrack(caseData)).isTrue();
    }

    @Test
    void shouldDetectDrhSmallClaim() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.NO);
        caseData.setSmallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing));

        assertThat(service.isDrhSmallClaim(caseData)).isTrue();
    }

    @Test
    void shouldDetectDrhSmallClaimFromAdditionalDirections() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setDrawDirectionsOrderSmallClaimsAdditionalDirections(
            List.of(SmallTrack.smallClaimDisputeResolutionHearing)
        );

        assertThat(service.isDrhSmallClaim(caseData)).isTrue();
    }

    @Test
    void shouldFallbackToFalseForNonMatchingCase() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setDrawDirectionsOrderRequired(YesOrNo.YES);
        caseData.setOrderType(OrderType.DECIDE_DAMAGES);

        assertThat(service.isSmallClaimsTrack(caseData)).isFalse();
        assertThat(service.isFastTrack(caseData)).isFalse();
        assertThat(service.isNihlFastTrack(caseData)).isFalse();
        assertThat(service.isDrhSmallClaim(caseData)).isFalse();
    }

    @Test
    void shouldDetectAdditionalParties() {
        Party applicant2 = new Party();
        applicant2.setType(Party.Type.INDIVIDUAL);
        Party respondent2 = new Party();
        respondent2.setType(Party.Type.INDIVIDUAL);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant2(applicant2);
        caseData.setRespondent2(respondent2);

        assertThat(service.hasApplicant2(caseData)).isTrue();
        assertThat(service.hasRespondent2(caseData)).isTrue();
    }

    @Test
    void shouldReportMissingAdditionalParties() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(service.hasApplicant2(caseData)).isFalse();
        assertThat(service.hasRespondent2(caseData)).isFalse();
    }
}
