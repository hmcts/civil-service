package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DjDocumentService {

    private final DefaultJudgmentOrderFormGenerator defaultJudgmentOrderFormGenerator;
    private final AssignCategoryId assignCategoryId;

    public Optional<CaseDocument> generateOrder(CaseData caseData, String authToken) {
        log.info("Generate DJ order request for caseId {}", caseData.getCcdCaseReference());
        return Optional.ofNullable(defaultJudgmentOrderFormGenerator.generate(caseData, authToken));
    }

    public void assignCategory(CaseDocument document, String categoryId) {
        assignCategoryId.assignCategoryIdToCaseDocument(document, categoryId);
    }
}
