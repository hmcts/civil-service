package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;

public class RespondToClaimHelper {

    public static void addResponseDocuments(ResponseDocument respondent1ResponseDocument,
                                            ResponseDocument respondent2ResponseDocument,
                                            CaseData caseData,
                                            CaseData.CaseDataBuilder<?, ?> updatedCaseData,
                                            AssignCategoryId assignCategoryId) {
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        if (respondent1ResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1ResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                defendantUploads.add(
                    buildElemCaseDocument(respondent1ClaimDocument, "Defendant",
                                          updatedCaseData.build().getRespondent1ResponseDate(),
                                          DocumentType.DEFENDANT_DEFENCE
                    ));
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1ClaimDocument,
                    "defendant1DefenseDirectionsQuestionnaire"
                );
            }
        }
        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        if (respondent1DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1DQDraftDirections = respondent1DQ.getRespondent1DQDraftDirections();
            if (respondent1DQDraftDirections != null) {
                defendantUploads.add(
                    buildElemCaseDocument(
                        respondent1DQDraftDirections,
                        "Defendant",
                        updatedCaseData.build().getRespondent1ResponseDate(),
                        DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                    ));
                assignCategoryId.assignCategoryIdToDocument(
                    respondent1DQDraftDirections,
                    "defendant1DefenseDirectionsQuestionnaire"
                );
            }
        }
        if (respondent2ResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2Document = respondent2ResponseDocument.getFile();
            if (respondent2Document != null) {
                defendantUploads.add(
                    buildElemCaseDocument(respondent2Document, "Defendant 2",
                                          updatedCaseData.build().getRespondent2ResponseDate(),
                                          DocumentType.DEFENDANT_DEFENCE
                    ));
                assignCategoryId.assignCategoryIdToDocument(respondent2Document,
                                                            "defendant2DefenseDirectionsQuestionnaire");
            }
        }
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        if (respondent2DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2DQDraftDirections = respondent2DQ.getRespondent2DQDraftDirections();
            if (respondent2DQDraftDirections != null) {
                defendantUploads.add(
                    buildElemCaseDocument(
                        respondent2DQDraftDirections,
                        "Defendant 2",
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                    ));
                assignCategoryId.assignCategoryIdToDocument(respondent2DQDraftDirections,
                                                            "defendant2DefenseDirectionsQuestionnaire");
            }
        }
        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
    }
}
