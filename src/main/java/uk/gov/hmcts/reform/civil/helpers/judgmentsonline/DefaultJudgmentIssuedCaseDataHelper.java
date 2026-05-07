package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType.DEFAULT_JUDGMENT;

public final class DefaultJudgmentIssuedCaseDataHelper {

    private DefaultJudgmentIssuedCaseDataHelper() {
        // Utility class.
    }

    public static boolean isFinalOrdersIssuedDefaultJudgment(CaseData caseData) {
        return caseData != null
            && CaseState.All_FINAL_ORDERS_ISSUED.equals(caseData.getCcdState())
            && hasIssuedDefaultJudgment(caseData);
    }

    private static boolean hasIssuedDefaultJudgment(CaseData caseData) {
        return hasIssuedActiveDefaultJudgment(caseData) || hasDefaultJudgmentDocument(caseData);
    }

    private static boolean hasIssuedActiveDefaultJudgment(CaseData caseData) {
        return Optional.ofNullable(caseData.getActiveJudgment())
            .map(activeJudgment -> DEFAULT_JUDGMENT.equals(activeJudgment.getType())
                && JudgmentState.ISSUED.equals(activeJudgment.getState()))
            .orElse(false);
    }

    private static boolean hasDefaultJudgmentDocument(CaseData caseData) {
        return Optional.ofNullable(caseData.getDefaultJudgmentDocuments())
            .orElse(List.of())
            .stream()
            .filter(Objects::nonNull)
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .anyMatch(DefaultJudgmentIssuedCaseDataHelper::isDefaultJudgmentDocument);
    }

    private static boolean isDefaultJudgmentDocument(CaseDocument document) {
        return DocumentType.DEFAULT_JUDGMENT.equals(document.getDocumentType());
    }
}
