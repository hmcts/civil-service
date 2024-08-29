package uk.gov.hmcts.reform.civil.utils;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.DQ;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

@Component
@AllArgsConstructor
public class DQResponseDocumentUtils {

    private static final String CLAIMANT = "Claimant";

    private final AssignCategoryId assignCategoryId;

    public List<Element<CaseDocument>> buildClaimantResponseDocuments(CaseData caseData) {
        return buildDQResponseDocuments(
                caseData.getApplicant1DQ(),
                CLAIMANT,
                DocCategory.DQ_APP1,
                caseData.getApplicant1ResponseDate());
    }

    private List<Element<CaseDocument>> buildDQResponseDocuments(DQ dq, String createdBy, DocCategory docCategory, LocalDateTime date) {
        List<Element<CaseDocument>> documents = new ArrayList<>();

        if (!nonNull(dq)) {
            return documents;
        }

        if (nonNull(dq.getDraftDirections())) {
            documents.add(buildElemCaseDocument(dq.getDraftDirections(), createdBy, date, DocumentType.CLAIMANT_DRAFT_DIRECTIONS));
        }

        if (!documents.isEmpty()) {
            assignCategoryId.assignCategoryIdToCollection(
                documents,
                document -> document.getValue().getDocumentLink(),
                docCategory.getValue()
            );
        }
        return documents;
    }

}
