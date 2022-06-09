package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
@RequiredArgsConstructor
@Configuration
@ComponentScan("uk.gov.hmcts.reform")
public class ClaimFormService {

    @Autowired
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    @PostMapping("/generateSealedDoc")
    public CaseDocument uploadSealedDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation, @NotNull @RequestBody CaseData caseData) {
        return sealedClaimFormGeneratorForSpec.generate(caseData, authorisation);
    }

    @PostMapping(value = "/downloadSealedDoc",
        produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    byte[] downloadSealedDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @NotNull @RequestBody CaseDocument caseDocument) {
        return sealedClaimFormGeneratorForSpec.downloadDocument(caseDocument, authorisation);
    }
}
