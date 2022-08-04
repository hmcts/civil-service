package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.controllers.testingsupport.UpdateCaseDataController;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import javax.validation.constraints.NotNull;

import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(
    path = "/case/document"
)
public class DocumentController {

    @Autowired
    private final ClaimFormService claimFormService;

    @Autowired
    private final UpdateCaseDataController updateCaseDataController;

    @Autowired
    private SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    @PostMapping("/generateSealedDoc/{caseId}")
    public CaseDocument uploadSealedDocument(@PathVariable("caseId") Long caseId,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation, @NotNull @RequestBody CaseData caseData) {
        CaseDocument caseDocument = claimFormService.uploadSealedDocument(authorisation, caseData);
        System.out.println(" CaseDocument name " +  caseDocument.getDocumentName());
        System.out.println("------------------------------------" + caseData);
        caseData = caseData.toBuilder().systemGeneratedCaseDocuments(ElementUtils.wrapElements(caseDocument))
           .build();
       System.out.println("provided case id " + caseId);

       updateCaseDataController.updateCaseDataSpecData(caseId, caseData);
        return caseDocument;

    }

    @PostMapping(value = "/downloadSealedDoc",
        produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    byte[] downloadSealedDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @NotNull @RequestBody CaseDocument caseDocument) {
        return claimFormService.downloadSealedDocument(authorisation, caseDocument);
    }
}
