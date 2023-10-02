package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

@Slf4j
@Service
@RequiredArgsConstructor
@Configuration
@ComponentScan("uk.gov.hmcts.reform")
public class ClaimFormService {

    private final DocumentDownloadService documentDownloadService;

    @Autowired
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    public CaseDocument uploadSealedDocument(
         String authorisation, CaseData caseData) {
        return sealedClaimFormGeneratorForSpec.generate(caseData, authorisation);
    }

    public DownloadedDocumentResponse downloadDocumentById(String authorisation, String caseDocumentId) {
        return documentDownloadService.downloadDocument(authorisation, caseDocumentId);
    }
}
