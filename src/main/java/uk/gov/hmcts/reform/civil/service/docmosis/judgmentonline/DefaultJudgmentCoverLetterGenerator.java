package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.DefaultJudgmentDefendantLrCoverLetter;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.applicant2Present;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getOrganisationByPolicy;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.respondent2Present;

@Slf4j
@RequiredArgsConstructor
@Service
public class DefaultJudgmentCoverLetterGenerator {

    private final OrganisationService organisationService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendCoverLetterToDefendantLR";
    private static final String DEFAULT_JUDGMENT_COVER_LETTER = "default-judgment-cover-letter";

    public byte[] generateAndPrintDjCoverLetters(CaseData caseData, String authorisation, boolean toSecondLegalOrg) {
        DocmosisDocument coverLetter = generateCoverLetter(caseData, toSecondLegalOrg);
        CaseDocument coverLetterCaseDocument =  documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG.getDocumentTitle(),
                coverLetter.getBytes(),
                DocumentType.DEFAULT_JUDGMENT_COVER_LETTER
            )
        );
        String documentUrl = coverLetterCaseDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;
        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Default Judgment Cover Letter ");
            throw new DocumentDownloadException(coverLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
        }

        List<String> recipients = getRecipientsList(caseData, toSecondLegalOrg);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                caseData.getLegacyCaseReference(), DEFAULT_JUDGMENT_COVER_LETTER, recipients);
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData, boolean toSecondLegalOrg) {
        Optional <Organisation> organisationOp = toSecondLegalOrg ?
            getOrganisationByPolicy(caseData.getRespondent2OrganisationPolicy(), this.organisationService)
            : getOrganisationByPolicy(caseData.getRespondent1OrganisationPolicy(), this.organisationService);
        return organisationOp.map(Organisation::getName).stream().toList();
    }

    private DocmosisDocument generateCoverLetter(CaseData caseData, boolean toSecondLegalOrg) {
        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData, toSecondLegalOrg),
            DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG
        );
    }

    public DefaultJudgmentDefendantLrCoverLetter getTemplateData(CaseData caseData, boolean toSecondLegalOrg) {
        Optional <Organisation> organisationOp = toSecondLegalOrg ?
            getOrganisationByPolicy(caseData.getRespondent2OrganisationPolicy(), this.organisationService)
            : getOrganisationByPolicy(caseData.getRespondent1OrganisationPolicy(), this.organisationService);

        if(organisationOp.isPresent()) {
            Organisation organisation = organisationOp.get();
            StringBuilder claimantName = new StringBuilder().append(caseData.getApplicant1().getPartyName());
            claimantName.append(applicant2Present(caseData)
                                    ? " and " + caseData.getApplicant2().getPartyName() : "");
            StringBuilder defendantName = new StringBuilder().append(caseData.getRespondent1().getPartyName());
            defendantName.append(respondent2Present(caseData)
                                     ? " and " + caseData.getRespondent2().getPartyName() : "");

            return DefaultJudgmentDefendantLrCoverLetter
                .builder()
                .claimReferenceNumber(caseData.getLegacyCaseReference())
                .legalOrgName(organisation.getName())
                .addressLine1(organisation.getContactInformation().get(0).getAddressLine1())
                .addressLine2(organisation.getContactInformation().get(0).getAddressLine2())
                .townCity(organisation.getContactInformation().get(0).getTownCity())
                .postCode(organisation.getContactInformation().get(0).getPostCode())
                .defendantName(defendantName.toString())
                .claimantName(claimantName.toString())
                .issueDate(LocalDate.now())
                .build();
        }
        return null;
    }

}
