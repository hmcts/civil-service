package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis.GenerateClaimFormForSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim.SealedClaimFormGeneratorForSpec;
import uk.gov.hmcts.reform.civil.service.stitching.CivilDocumentStitchingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor
@Configuration
@ComponentScan("uk.gov.hmcts.reform")
public class ClaimFormService {

    private final CivilDocumentStitchingService civilDocumentStitchingService;
    private final GenerateClaimFormForSpecCallbackHandler generateClaimFormForSpecCallbackHandler;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;

    public CaseDocument uploadSealedDocument(
        String authorisation, CaseData caseData) {
        LocalDate issueDate = time.now().toLocalDate();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate)
            .respondent1ResponseDeadline(
                deadlinesCalculator.plus14DaysAt4pmDeadline(LocalDateTime.now()))
            // .respondent1Represented(YES)
            .claimDismissedDate(null);
        CaseDocument sealClaimForm = sealedClaimFormGeneratorForSpec.generate(caseData, authorisation);

        List<DocumentMetaData> documentMetaDataList = generateClaimFormForSpecCallbackHandler
            .fetchDocumentsFromCaseData(caseData, sealClaimForm);

        if (documentMetaDataList.size() > 1) {
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                documentMetaDataList,
                authorisation,  //access token will be taken from postman
                sealClaimForm.getDocumentName(),
                sealClaimForm.getDocumentName(),
                caseData
            );
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(stitchedDocument));
            System.out.println("before building ");
            caseDataBuilder.build();
            System.out.println("after building ");
            if (stitchedDocument.getError() != null &&  !stitchedDocument.getError().isEmpty()) {
                return sealClaimForm;

            } else {
                if (stitchedDocument.getDocumentSize() > 1) {
                    return stitchedDocument;
                } else {
                    return sealClaimForm;
                }
            }

        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealClaimForm));
            System.out.println("before building ");
            caseDataBuilder.build();
            System.out.println("after building ");
            return sealClaimForm;
        }

    }

    @Autowired
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    public byte[] downloadSealedDocument(String authorisation, CaseDocument caseDocument) {
        return sealedClaimFormGeneratorForSpec.downloadDocument(caseDocument, authorisation);
    }
}
