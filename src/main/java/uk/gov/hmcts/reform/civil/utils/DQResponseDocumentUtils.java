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
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

@Component
@AllArgsConstructor
public class DQResponseDocumentUtils {

    private static final String CLAIMANT = "Claimant";
    private static final String DEFENDANT_1 = "Defendant 1";
    private static final String DEFENDANT_2 = "Defendant 2";

    private final AssignCategoryId assignCategoryId;

    public List<Element<CaseDocument>> buildClaimantResponseDocuments(CaseData caseData) {
        return buildDQResponseDocuments(
                caseData.getApplicant1DQ(),
                CLAIMANT,
                DocCategory.DQ_APP1,
                DocumentType.CLAIMANT_DRAFT_DIRECTIONS,
                caseData.getApplicant1ResponseDate());
    }

    public List<Element<CaseDocument>> buildDefendantResponseDocuments(CaseData caseData) {
        return Stream.concat(
            buildDefendant1ResponseDocuments(caseData).stream(),
            buildDefendant2ResponseDocuments(caseData).stream()
        ).toList();
    }

    public List<Element<CaseDocument>> buildDefendant1ResponseDocuments(CaseData caseData) {
        return buildDQResponseDocuments(
                caseData.getRespondent1DQ(),
                DEFENDANT_1,
                DocCategory.DQ_DEF1,
                DocumentType.DEFENDANT_DRAFT_DIRECTIONS,
                caseData.getRespondent1ResponseDate());
    }

    public List<Element<CaseDocument>> buildDefendant2ResponseDocuments(CaseData caseData) {
        return buildDQResponseDocuments(
            caseData.getRespondent2DQ(),
            DEFENDANT_2,
            DocCategory.DQ_DEF2,
            DocumentType.DEFENDANT_DRAFT_DIRECTIONS,
            caseData.getRespondent2ResponseDate());
    }

    private List<Element<CaseDocument>> buildDQResponseDocuments(DQ dq, String createdBy, DocCategory docCategory, DocumentType documentType, LocalDateTime date) {
        List<Element<CaseDocument>> documents = new ArrayList<>();

        if (!nonNull(dq)) {
            return documents;
        }

        if (nonNull(dq.getDraftDirections())) {
            documents.add(buildElemCaseDocument(dq.getDraftDirections(), createdBy, date, documentType));
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
