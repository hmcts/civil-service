package uk.gov.hmcts.reform.civil.helpers.settlediscontinue;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DiscontinueClaimHelperTest {

    @Test
    void shouldReturn_error_when_claim_is_1v1_LiPvLIP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1UnrepresentedDefendantSpec()
            .applicant1Represented(YesOrNo.NO)
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldReturn_error_when_claim_is_1v2_LiPvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedUnrepresentedDefendants()
            .applicant1Represented(YesOrNo.NO)
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldReturn_error_when_claim_is_1v2_LRvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2UnrepresentedDefendant()
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldReturn_error_when_claim_is_1v2_LiPvLR() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndSameRepresentative()
            .respondent2(PartyBuilder.builder().individual().build().toBuilder().build())
            .applicant1Represented(YesOrNo.NO)
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldReturn_error_when_claim_is_1v2_LRvLRvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldReturn_error_when_claim_is_1v2_LRvLiPvLR() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldReturn_error_when_claim_is_2v1_LiPvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted2v1RespondentUnrepresented()
            .applicant1Represented(YesOrNo.NO)
            .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors.get(0)).isEqualTo("This action is not available for this claim");
    }

    @Test
    void shouldNotReturn_error_when_claim_is_1v2_LRvLRWithDiffLR() {
        CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimSubmittedTwoRespondentRepresentatives()
                .respondent2(PartyBuilder.builder().individual().build().toBuilder().build())
                .build();
        List<String> errors = new ArrayList<>();
        DiscontinueClaimHelper.checkState(caseData, errors);
        assertThat(errors).isNotNull();
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturn_true_when_claim_is_1v2_LRvLR() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft().respondent2(Party.builder().partyName("Resp2").build())
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isTrue();
    }

    @Test
    void shouldReturn_false_when_claim_is_1v1_LiPvLIP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v1UnrepresentedDefendantSpec()
            .applicant1Represented(YesOrNo.NO)
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_1v2_LiPvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssuedUnrepresentedDefendants()
            .applicant1Represented(YesOrNo.NO)
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_1v2_LRvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2UnrepresentedDefendant()
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_1v2_LiPvLR() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndSameRepresentative()
            .respondent2(PartyBuilder.builder().individual().build().toBuilder().build())
            .applicant1Represented(YesOrNo.NO)
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_1v2_LRvLRvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented()
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_1v2_LRvLiPvLR() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented()
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_2v1_LiPvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted2v1RespondentUnrepresented()
            .applicant1Represented(YesOrNo.NO)
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }

    @Test
    void shouldReturn_false_when_claim_is_2v1_LRvLiP() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted2v1RespondentUnrepresented()
            .build();
        assertThat(DiscontinueClaimHelper.is1v2LrVLrCase(caseData)).isFalse();
    }
}
