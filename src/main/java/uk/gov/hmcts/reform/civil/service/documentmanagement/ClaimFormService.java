package uk.gov.hmcts.reform.civil.service.documentmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.CaseDetails;
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
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final GenerateClaimFormForSpecCallbackHandler generateClaimFormForSpecCallbackHandler;
    private final ObjectMapper objectMapper;

    public CaseDocument uploadSealedDocument(
        String authorisation, CaseData caseData) {
        log.info(" Called End point");
        LocalDate issueDate = time.now().toLocalDate();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder().issueDate(issueDate)
            .respondent1ResponseDeadline(
                deadlinesCalculator.plus14DaysAt4pmDeadline(LocalDateTime.now()))
            // .respondent1Represented(YES)
            .claimDismissedDate(null);
        log.info("before calling seal claim generator");
        CaseDocument sealClaimForm = sealedClaimFormGeneratorForSpec.generate(caseData, authorisation);
        log.info(" sealClaimForm document name " + sealClaimForm.getDocumentName());
        log.info(" sealClaimForm document size" + sealClaimForm.getDocumentSize());
        log.info(" sealClaimForm document link" + sealClaimForm.getDocumentLink());

        log.info("before calling fetch document from case data");
        List<DocumentMetaData> documentMetaDataList = generateClaimFormForSpecCallbackHandler
            .fetchDocumentsFromCaseData(caseData, sealClaimForm);

        log.info("fetch document, documentMetaDataList size " + documentMetaDataList.size());

        if (documentMetaDataList.size() >= 1) {
            log.info("since size is greater or equal to 1");
            CaseDocument stitchedDocument = civilDocumentStitchingService.bundle(
                documentMetaDataList,
                authorisation,  //access token will be taken from postman
                sealClaimForm.getDocumentName(),
                sealClaimForm.getDocumentName(),
                caseData
            );
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(stitchedDocument));
            log.info("before building if ");
            CaseDetails.builder().data(caseDataBuilder.build().toMap(objectMapper))
               .build();
            log.info("after building if");
            if (stitchedDocument.getError() != null &&  !stitchedDocument.getError().isEmpty()) {
                log.info("There is issue with Stitching");
                return sealClaimForm;

            } else {
                if (stitchedDocument.getDocumentSize() > 1) {
                    log.info("Document been stitched okay");
                    return stitchedDocument;
                } else {
                    return sealClaimForm;
                }
            }

        } else {
            caseDataBuilder.systemGeneratedCaseDocuments(wrapElements(sealClaimForm));
            log.info("before building else ");
            CaseDetails.builder().data(caseDataBuilder.build().toMap(objectMapper)).build();
            log.info("after building else");
            return sealClaimForm;
        }

    }

    @Autowired
    private final SealedClaimFormGeneratorForSpec sealedClaimFormGeneratorForSpec;

    public byte[] downloadSealedDocument(String authorisation, CaseDocument caseDocument) {
        return sealedClaimFormGeneratorForSpec.downloadDocument(caseDocument, authorisation);
    }
}
