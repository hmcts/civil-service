package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoDocumentService {

    private final SdoGeneratorService sdoGeneratorService;
    private final AssignCategoryId assignCategoryId;

    public Optional<CaseDocument> generateSdoDocument(CaseData caseData, String authToken) {
        log.info("Generate SDO doc request for caseId {}", caseData.getCcdCaseReference());
        return Optional.ofNullable(sdoGeneratorService.generate(caseData, authToken));
    }

    public void assignCategory(CaseDocument document, String categoryId) {
        assignCategoryId.assignCategoryIdToCaseDocument(document, categoryId);
    }
}
