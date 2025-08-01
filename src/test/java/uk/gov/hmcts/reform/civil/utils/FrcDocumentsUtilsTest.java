package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FrcDocumentsUtilsTest {

    @Spy
    private AssignCategoryId assignCategoryId = new AssignCategoryId();
    @InjectMocks
    private FrcDocumentsUtils frcDocumentsUtils;

    @Test
    void shouldNotSetDefendantsFRCDocumentsWhenNoDocs_1v2DS() {
        CaseData caseData = CaseDataBuilder.builder()
            .setIntermediateTrackClaim()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent1DQWithFixedRecoverableCostsIntermediate(null)
            .build();

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        assertThat(caseData.getRespondent1DQ().getRespondent1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument())
            .isEqualTo(null);
    }

    @Test
    void shouldSetDefendantsFRCDocuments_1v1() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateRespondentFullDefence()
            .setIntermediateTrackClaim()
            .respondent1DQWithFixedRecoverableCostsIntermediate()
            .build();

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        assertThat(caseData.getRespondent1DQ().getRespondent1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument().getCategoryID())
            .isEqualTo(DocCategory.DQ_DEF1.getValue());
    }

    @Test
    void shouldSetClaimantsFRCDocuments() {
        CaseData caseData = CaseDataBuilder.builder()
                .setIntermediateTrackClaim()
                .atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.TWO_V_ONE)
                .multiPartyClaimTwoApplicants()
                .setIntermediateTrackClaim()
                .applicant1DQWithFixedRecoverableCostsIntermediate()
                .build();

        frcDocumentsUtils.assembleClaimantsFRCDocuments(caseData);

        assertThat(caseData.getApplicant1DQ().getApplicant1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument().getCategoryID())
                .isEqualTo("DQApplicant");
    }

    @Test
    void shouldNotSetDefendantsFRCDocuments() {
        CaseData caseData = CaseDataBuilder.builder()
            .setIntermediateTrackClaim()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
            .respondent1DQWithFixedRecoverableCostsIntermediate()
            .respondent2DQWithFixedRecoverableCostsIntermediate()
            .build();

        frcDocumentsUtils.assembleDefendantsFRCDocuments(caseData);

        assertThat(caseData.getRespondent1DQ().getRespondent1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument().getCategoryID())
            .isEqualTo(null);
        assertThat(caseData.getRespondent2DQ().getRespondent2DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument().getCategoryID())
            .isEqualTo(null);
    }
}
