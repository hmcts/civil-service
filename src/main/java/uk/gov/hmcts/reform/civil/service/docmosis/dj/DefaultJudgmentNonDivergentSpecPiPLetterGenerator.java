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
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.DefaultJudgmentNonDivergentSpecLipDefendantLetter;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SET_ASIDE_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_ORDER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultJudgmentNonDivergentSpecPiPLetterGenerator {

    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    private final PinInPostConfiguration pipInPostConfiguration;
    private final GeneralAppFeesService generalAppFeesService;
    private static final String DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER_REF = "default-judgment-non-divergent-spec-pin_in_letter";

    public byte[] generateAndPrintDefaultJudgementSpecLetter(CaseData caseData, String authorisation) {
        DocmosisDocument defaultJudgmentNonDivergentPinInLetter = generate(caseData);
        CaseDocument defaultJudgmentNonDivergentPinInLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER.getDocumentTitle(),
                defaultJudgmentNonDivergentPinInLetter.getBytes(),
                DocumentType.DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER
            )
        );

        String documentUrl = defaultJudgmentNonDivergentPinInLetterCaseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Default Judgment Non-Divergent PIN In Letter Defendant1 LiP");
            throw new DocumentDownloadException(defaultJudgmentNonDivergentPinInLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_IN_LETTER_REF, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private DocmosisDocument generate(CaseData caseData) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData),
            DEFAULT_JUDGMENT_NON_DIVERGENT_SPEC_PIN_LIP_DEFENDANT_LETTER
        );
    }

    public DefaultJudgmentNonDivergentSpecLipDefendantLetter getTemplateData(CaseData caseData) {
        return DefaultJudgmentNonDivergentSpecLipDefendantLetter
            .builder()
            .claimReferenceNumber(caseData.getLegacyCaseReference())
            .claimantName(caseData.getApplicant1().getPartyName())
            .defendant(caseData.getRespondent1())
            .letterIssueDate(LocalDate.now())
            .caseSubmittedDate(caseData.getSubmittedDate().toLocalDate())
            .pin(caseData.getRespondent1PinToPostLRspec().getAccessCode())
            .respondToClaimUrl(pipInPostConfiguration.getRespondToClaimUrl())
            .varyJudgmentFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(VARY_ORDER).formData()))
            .judgmentSetAsideFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(SET_ASIDE_JUDGEMENT).formData()))
            .certifOfSatisfactionFee(String.valueOf(generalAppFeesService.getFeeForJOWithApplicationType(OTHER).formData()))
            .build();
    }
}
