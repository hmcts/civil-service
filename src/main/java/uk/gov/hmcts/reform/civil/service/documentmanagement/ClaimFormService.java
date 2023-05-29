package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

@Slf4j
@Service
@RequiredArgsConstructor
@Configuration
@ComponentScan("uk.gov.hmcts.reform")
public class ClaimFormService {

    public CaseDocument uploadSealedDocument(
         String authorisation, CaseData caseData) {
        return sealedClaimFormGeneratorForSpec.generate(caseData, authorisation);
    }

    @Autowired
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    public byte[] downloadDocumentById(String authorisation, CaseDocument caseDocument) {
        return sealedClaimFormGeneratorForSpec.downloadDocumentById(caseDocument, authorisation);
    }

    public ResponseEntity<Resource> downloadDocumentById(String caseDocumentId) {
        return sealedClaimFormGeneratorForSpec.downloadDocumentById(caseDocumentId);
    }
}
