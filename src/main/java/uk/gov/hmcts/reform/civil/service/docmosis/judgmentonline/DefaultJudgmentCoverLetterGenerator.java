package uk.gov.hmcts.reform.civil.service.docmosis.judgmentonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.judgmentonline.DefaultJudgmentDefendantLrCoverLetter;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

    private final CivilStitchService civilStitchService;
    private final OrganisationService organisationService;
    private final DocumentGeneratorService documentGeneratorService;
    private final DocumentManagementService documentManagementService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    public static final String TASK_ID = "SendCoverLetterToDefendantLR";
    private static final String DEFAULT_JUDGMENT_COVER_LETTER = "default-judgment-cover-letter";

    @Value("${stitching.enabled:true}")
    private boolean stitchEnabled;

    public byte[] generateAndPrintDjCoverLettersPlusDocument(CaseData caseData, String authorisation, boolean toSecondLegalOrg) {
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating default judgement cover letter with documents for caseId{}", caseId);
        byte[] letterContent = new byte[0];
        if (stitchEnabled) {
            DocmosisDocument coverLetter = generateCoverLetter(caseData, toSecondLegalOrg);
            CaseDocument coverLetterCaseDocument = documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                    DEFAULT_JUDGMENT_COVER_LETTER_DEFENDANT_LEGAL_ORG.getDocumentTitle(),
                    coverLetter.getBytes(),
                    DocumentType.DEFAULT_JUDGMENT_COVER_LETTER
                )
            );

            List<DocumentMetaData> letterAndDocumentMetaDataList = fetchDocumentsFromCaseData(
                caseData,
                coverLetterCaseDocument,
                toSecondLegalOrg ? DocumentType.DEFAULT_JUDGMENT_DEFENDANT2 : DocumentType.DEFAULT_JUDGMENT_DEFENDANT1
            );

            log.info("Calling civil stitch service from judgement cover letter for caseId {}", caseId);
            CaseDocument stitchedDocuments =
                civilStitchService.generateStitchedCaseDocument(letterAndDocumentMetaDataList,
                                                                coverLetterCaseDocument.getDocumentName(),
                                                                caseId,
                                                                toSecondLegalOrg ? DocumentType.DEFAULT_JUDGMENT_DEFENDANT2
                                                                    : DocumentType.DEFAULT_JUDGMENT_DEFENDANT1,
                                                                authorisation);

            log.info("Response judgement cover letter {} for caseId {}", stitchedDocuments, caseId);

            try {
                String documentUrl = stitchedDocuments.getDocumentLink().getDocumentUrl();
                String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
                letterContent = documentDownloadService.downloadDocument(
                    authorisation,
                    documentId
                ).file().getInputStream().readAllBytes();
            } catch (IOException e) {
                log.error("Failed getting letter content for Default Judgment Cover Letter for caseId {}", caseId, e);
                throw new DocumentDownloadException(coverLetterCaseDocument.getDocumentLink().getDocumentFileName(), e);
            }

            List<String> recipients = getRecipientsList(caseData, toSecondLegalOrg);
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                         caseData.getLegacyCaseReference(), DEFAULT_JUDGMENT_COVER_LETTER, recipients
            );
        } else {
            log.error("Failed generating Cover Letter for Default Judgment - stitch disabled for caseId {}", caseId);
        }
        return letterContent;
    }

    private List<String> getRecipientsList(CaseData caseData, boolean toSecondLegalOrg) {
        Optional<Organisation> organisationOp = toSecondLegalOrg
            ? getOrganisationByPolicy(caseData.getRespondent2OrganisationPolicy(), this.organisationService)
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
        Optional<Organisation> organisationOp = toSecondLegalOrg
            ? getOrganisationByPolicy(caseData.getRespondent2OrganisationPolicy(), this.organisationService)
            : getOrganisationByPolicy(caseData.getRespondent1OrganisationPolicy(), this.organisationService);

        if (organisationOp.isPresent()) {
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

    private List<DocumentMetaData> fetchDocumentsFromCaseData(CaseData caseData, CaseDocument caseDocument, DocumentType requiredDocumentType) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(caseDocument.getDocumentLink(),
                                                      "Cover Letter",
                                                      LocalDate.now().toString()));

        Optional<Element<CaseDocument>> optionalSealedDocument = caseData.getDefaultJudgmentDocuments().stream()
            .filter(defaultJudgmentDocument -> defaultJudgmentDocument.getValue()
                .getDocumentType().equals(requiredDocumentType)).sorted(Comparator.comparing(
                    caseDocumentElement -> caseDocumentElement.getValue().getCreatedDatetime(),
                    Comparator.reverseOrder())).findFirst();

        optionalSealedDocument.ifPresent(caseDocumentElement -> documentMetaDataList.add(new DocumentMetaData(
            caseDocumentElement.getValue().getDocumentLink(),
            "Default Judgment Defendant document",
            LocalDate.now().toString()
        )));
        return documentMetaDataList;
    }
}
