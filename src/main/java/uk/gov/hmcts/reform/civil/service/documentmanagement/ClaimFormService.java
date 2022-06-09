package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

@Slf4j
@Service
@RequiredArgsConstructor
@Configuration
@ComponentScan("uk.gov.hmcts.reform")
public class ClaimFormService {

    @Autowired
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    public byte[] downloadSealedDocument(String authorisation, CaseDocument caseDocument) {
        return sealedClaimFormGeneratorForSpec.downloadDocument(caseDocument, authorisation);
    }
}
