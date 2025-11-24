package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;

@Component
@AllArgsConstructor
public class FrcDocumentsUtils {

    private final FeatureToggleService featureToggleService;
    private final AssignCategoryId assignCategoryId;

    public void assembleDefendantsFRCDocuments(CaseData caseData) {
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && !INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())) {
            return;
        }

        if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getFixedRecoverableCostsIntermediate)
            .map(FixedRecoverableCosts::getFrcSupportingDocument).isPresent()) {

            Document respondent1FrcSupportingDocument = caseData
                .getRespondent1DQ().getRespondent1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument();

            assignCategoryId.assignCategoryIdToDocument(respondent1FrcSupportingDocument,
                                                        DocCategory.DQ_DEF1.getValue());
        }

        if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getFixedRecoverableCostsIntermediate)
            .map(FixedRecoverableCosts::getFrcSupportingDocument).isPresent()) {

            Document respondent2FrcSupportingDocument = caseData
                .getRespondent2DQ().getRespondent2DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument();

            assignCategoryId.assignCategoryIdToDocument(respondent2FrcSupportingDocument,
                                                        DocCategory.DQ_DEF2.getValue());
        }
    }

    public void assembleClaimantsFRCDocuments(CaseData caseData) {
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)
            && !INTERMEDIATE_CLAIM.equals(caseData.getAllocatedTrack())) {
            return;
        }

        if (Optional.ofNullable(caseData.getApplicant1DQ())
            .map(Applicant1DQ::getFixedRecoverableCostsIntermediate)
            .map(FixedRecoverableCosts::getFrcSupportingDocument).isPresent()) {

            Document frcSupportingDocument = caseData
                .getApplicant1DQ().getApplicant1DQFixedRecoverableCostsIntermediate().getFrcSupportingDocument();

            assignCategoryId.assignCategoryIdToDocument(frcSupportingDocument,
                                                        DocCategory.DQ_APP1.getValue());
        }
    }
}
