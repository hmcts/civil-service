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
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.JudgmentByAdmissionLiPDefendantLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final CivilStitchService civilStitchService;

    public static final String TASK_ID = "SendJudgmentByAdmissionLiPLetterDef1";
    private static final String JUDGMENT_BY_ADMISSION_LETTER = "judgment-by-admission-letter";

    public byte[] generateAndPrintJudgmentByAdmissionLetter(CaseData caseData, String authorisation) {
        DocmosisDocument judgmentByAdmissionLetter = generate(caseData);
        CaseDocument pinAndPostcaseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                JUDGMENT_BY_ADMISSION_PIN_IN_POST_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                judgmentByAdmissionLetter.getBytes(),
                DocumentType.JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER
            )
        );
        CaseDocument caseDocument = getDefendantJbaDocStitchedToPinAndPostDoc(
            caseData,
            pinAndPostcaseDocument,
            authorisation
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

    private CaseDocument getDefendantJbaDocStitchedToPinAndPostDoc(CaseData caseData, CaseDocument pinAndPostLetterDoc, String authorisation) {
        if (!caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            CaseDocument defendantJbaDoc = caseData.getSystemGeneratedCaseDocuments().stream()
                .map(Element::getValue)
                .filter(doc -> doc.getDocumentType().equals(DocumentType.JUDGMENT_BY_ADMISSION_DEFENDANT))
                .findFirst()
                .orElse(null);
            if (defendantJbaDoc != null) {
                List<DocumentMetaData> documentMetaDataList = appendDefendantJbaDocToPinAndPostDoc(
                    pinAndPostLetterDoc,
                    defendantJbaDoc
                );
                Long caseId = caseData.getCcdCaseReference();
                log.info("Calling stitching service");
                return civilStitchService.generateStitchedCaseDocument(
                    documentMetaDataList,
                    pinAndPostLetterDoc.getDocumentName(),
                    caseId,
                    DocumentType.JUDGMENT_BY_ADMISSION_NON_DIVERGENT_SPEC_PIP_LETTER,
                    authorisation
                );
            }
        }
        return pinAndPostLetterDoc;
    }

    private List<DocumentMetaData> appendDefendantJbaDocToPinAndPostDoc(CaseDocument pinAndPostLetterDoc, CaseDocument defendantJbaDoc) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();
        log.info("Append defendant JBA doc to pin and post letter doc");
        documentMetaDataList.add(new DocumentMetaData(
            pinAndPostLetterDoc.getDocumentLink(),
            "Pin and Post Letter Document",
            LocalDate.now().toString()
        ));
        documentMetaDataList.add(new DocumentMetaData(
            defendantJbaDoc.getDocumentLink(),
            "Defendant JBA Doc to attach",
            LocalDate.now().toString()
        ));
        return documentMetaDataList;
    }
}
