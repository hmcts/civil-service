package uk.gov.hmcts.reform.civil.controllers.cases;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.service.documentmanagement.ClaimFormService;

import javax.validation.constraints.NotNull;

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

    @PostMapping("/generateSealedDoc")
    public CaseDocument uploadSealedDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation, @NotNull @RequestBody CaseData caseData) {
        return claimFormService.uploadSealedDocument(authorisation, caseData);
    }

    @PostMapping(value = "/downloadSealedDoc",
        produces = MediaType.APPLICATION_PDF_VALUE)
    public @ResponseBody
    byte[] downloadSealedDocument(
        @NotNull @RequestBody CaseDocument caseDocument) {
        return claimFormService.downloadSealedDocument(caseDocument);
    }

}
