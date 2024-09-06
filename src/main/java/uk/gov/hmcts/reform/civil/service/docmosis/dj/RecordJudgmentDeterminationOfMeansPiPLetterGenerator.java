package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.RecordJudgmentDeterminationOfMeansLiPDefendantLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class RecordJudgmentDeterminationOfMeansPiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final GeneralAppFeesService generalAppFeesService;
    private static final String RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER = "record-judgment-determination-of-means-letter";

    public byte[] generateAndPrintRecordJudgmentDeterminationOfMeansLetter(CaseData caseData, String authorisation) {
        DocmosisDocument recordJudgmentDeterminationOfMeansLetter = generate(caseData);
        CaseDocument recordJudgmentDeterminationOfMeansLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                recordJudgmentDeterminationOfMeansLetter.getBytes(),
                DocumentType.RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER
            )
        );
        String documentUrl = recordJudgmentDeterminationOfMeansLetterCaseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Record Judgment Determination Of Means LiP Letter ");
            throw new DocumentDownloadException(recordJudgmentDeterminationOfMeansLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LETTER, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            RECORD_JUDGMENT_DETERMINATION_OF_MEANS_LIP_DEFENDANT_LETTER
        );
    }

    public RecordJudgmentDeterminationOfMeansLiPDefendantLetter getTemplateData(CaseData caseData) {
        return RecordJudgmentDeterminationOfMeansLiPDefendantLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .letterIssueDate(LocalDate.now())
            .pin(caseData.getRespondent1PinToPostLRspec().getAccessCode())
            .respondToClaimUrl(pipInPostConfiguration.getRespondToClaimUrl())
            .varyJudgmentFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(VARY_ORDER).formData()))
            .certifOfSatisfactionFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(OTHER).formData()))
            .build();
    }
}
