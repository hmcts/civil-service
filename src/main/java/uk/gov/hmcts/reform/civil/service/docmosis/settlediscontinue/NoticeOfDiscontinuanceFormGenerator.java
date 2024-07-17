package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue.NoticeOfDiscontinuanceForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.NOTICE_OF_DISCONTINUANCE_PDF;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeOfDiscontinuanceFormGenerator implements TemplateDataGenerator<NoticeOfDiscontinuanceForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generateDocs(CaseData caseData, String authorisation) {
        NoticeOfDiscontinuanceForm templateData = getNoticeOfDiscontinueData(caseData);
        DocmosisTemplates docmosisTemplate = NOTICE_OF_DISCONTINUANCE_PDF;
        DocmosisDocument docmosisDocument =
                documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        return documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                        getFileName(caseData, docmosisTemplate),
                        docmosisDocument.getBytes(),
                        DocumentType.NOTICE_OF_DISCONTINUANCE
                )
        );
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate) {
        return String.format(docmosisTemplate.getDocumentTitle(), caseData.getLegacyCaseReference());
    }

    private NoticeOfDiscontinuanceForm getNoticeOfDiscontinueData(CaseData caseData) {
        var noticeOfDiscontinueBuilder = NoticeOfDiscontinuanceForm.builder()
                .caseNumber(caseData.getLegacyCaseReference())
                .claimant1Name(caseData.getApplicant1().getPartyName())
                .claimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
                .defendant1Name(caseData.getRespondent1().getPartyName())
                .defendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
                .claimantNum(nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant")
                .defendantNum(nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant")
                .claimantWhoIsDiscontinue(getClaimantWhoIsDiscontinue(caseData))
                .claimantsConsentToDiscontinuance(nonNull(caseData.getClaimantsConsentToDiscontinuance())
                        ? caseData.getClaimantsConsentToDiscontinuance().toString() : null)
                .courtPermission(caseData.getCourtPermissionNeeded().getDisplayedValue())
                .permissionGranted(nonNull(caseData.getIsPermissionGranted()) ? caseData.getIsPermissionGranted().getDisplayedValue() : null)
                .judgeName(isCourtPermissionGranted(caseData)
                        ? caseData.getPermissionGrantedComplex().getPermissionGrantedJudge() : null)
                .judgementDate(isCourtPermissionGranted(caseData)
                        ? caseData.getPermissionGrantedComplex().getPermissionGrantedDate() : null)
                .typeOfDiscontinuance(caseData.getTypeOfDiscontinuance().toString())
                .typeOfDiscontinuanceTxt(caseData.getTypeOfDiscontinuance().getType())
                .partOfDiscontinuanceTxt(caseData.getPartDiscontinuanceDetails())
                .discontinuingAgainstOneDefendant(nonNull(caseData.getDiscontinuingAgainstOneDefendant())
                        ? caseData.getDiscontinuingAgainstOneDefendant().getValue().getLabel() : null)
                .discontinuingAgainstBothDefendants(nonNull(caseData.getIsDiscontinuingAgainstBothDefendants())
                        ? caseData.getIsDiscontinuingAgainstBothDefendants().getDisplayedValue() : null);
        return noticeOfDiscontinueBuilder.build();
    }

    private String getClaimantWhoIsDiscontinue(CaseData caseData) {
        return nonNull(caseData.getClaimantWhoIsDiscontinuing())
                ? caseData.getClaimantWhoIsDiscontinuing().getValue().getLabel()
                : caseData.getApplicant1().getPartyName();
    }

    private boolean isCourtPermissionGranted(CaseData caseData) {
        return nonNull(caseData.getIsPermissionGranted())
                && SettleDiscontinueYesOrNoList.YES.equals(caseData.getIsPermissionGranted());
    }
}

