package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

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
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.JudgmentByAdmissionLiPDefendantLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class JudgmentByAdmissionPiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final GeneralAppFeesService generalAppFeesService;

    public static final String TASK_ID = "SendJudgmentByAdmissionLiPLetterDef1";
    private static final String JUDGMENT_BY_ADMISSION_LETTER = "judgment-by-admission-letter";

    public byte[] generateAndPrintJudgmentByAdmissionLetter(CaseData caseData, String authorisation) {
        DocmosisDocument judgmentByAdmissionLetter = generate(caseData);
        CaseDocument caseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                judgmentByAdmissionLetter.getBytes(),
                DocumentType.JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER
            )
        );
        String documentUrl = caseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Judgment By Admission LiP Letter ");
            throw new DocumentDownloadException(caseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), JUDGMENT_BY_ADMISSION_LETTER, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER
        );
    }

    public JudgmentByAdmissionLiPDefendantLetter getTemplateData(CaseData caseData) {
        return JudgmentByAdmissionLiPDefendantLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .pin(caseData.getRespondent1PinToPostLRspec().getAccessCode())
            .respondToClaimUrl(pipInPostConfiguration.getRespondToClaimUrl())
            .varyJudgmentFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(VARY_ORDER).formData()))
            .certifOfSatisfactionFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(OTHER).formData()))
            .build();
    }
}
