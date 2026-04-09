package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

@Component
@Slf4j
public class AssembleDocumentsForDeadlineResponse {

    private final AssignCategoryId assignCategoryId;

    public AssembleDocumentsForDeadlineResponse(AssignCategoryId assignCategoryId) {
        this.assignCategoryId = assignCategoryId;
    }

    void assembleResponseDocuments(CaseData caseData) {
        log.info("Assembling response documents for case ID: {}", caseData.getCcdCaseReference());
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        assembleRespondent1ResponseDocuments(caseData, defendantUploads);
        assembleRespondent2ResponseDocuments(caseData, defendantUploads);
    }

    private void assembleRespondent2ResponseDocuments(CaseData caseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent2ClaimResponseDocument = caseData.getRespondent2ClaimResponseDocument();
        if (respondent2ClaimResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2ClaimResponseDocument.getFile();
            if (respondent2ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent2ClaimDocument, "Defendant 2",
                    caseData.getRespondent2ResponseDate(),
                    DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent2ClaimDocument,
                    DocCategory.DEF2_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        if (respondent2DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2DQDraftDirections = respondent2DQ.getRespondent2DQDraftDirections();
            if (respondent2DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent2DQDraftDirections,
                    "Defendant 2",
                    caseData.getRespondent2ResponseDate(),
                    DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent2DQDraftDirections,
                    DocCategory.DQ_DEF2.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }

        if (!defendantUploads.isEmpty()) {
            caseData.setDefendantResponseDocuments(defendantUploads);
        }
    }

    private void assembleRespondent1ResponseDocuments(CaseData caseData, List<Element<CaseDocument>> defendantUploads) {
        ResponseDocument respondent1ClaimResponseDocument = caseData.getRespondent1ClaimResponseDocument();
        if (respondent1ClaimResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1ClaimResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                Element<CaseDocument> documentElement =
                    buildElemCaseDocument(respondent1ClaimDocument, "Defendant",
                                          caseData.getRespondent1ResponseDate(),
                                          DocumentType.DEFENDANT_DEFENCE
                    );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1ClaimDocument,
                    DocCategory.DEF1_DEFENSE_DQ.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }

        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        if (respondent1DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1DQDraftDirections = respondent1DQ.getRespondent1DQDraftDirections();
            if (respondent1DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                    respondent1DQDraftDirections,
                    "Defendant",
                    caseData.getRespondent1ResponseDate(),
                    DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1DQDraftDirections,
                    DocCategory.DQ_DEF1.getValue()
                );
                defendantUploads.add(documentElement);
            }
        }
    }
}
