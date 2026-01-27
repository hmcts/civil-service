package uk.gov.hmcts.reform.civil.service.docmosis.settlediscontinue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.settleanddiscontinue.NoticeOfDiscontinuanceForm;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import java.time.LocalDate;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingDetailsMapper.isWelshHearingSelected;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.NOTICE_OF_DISCONTINUANCE_PDF;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeOfDiscontinuanceFormGenerator implements TemplateDataGenerator<NoticeOfDiscontinuanceForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final FeatureToggleService featureToggleService;

    public CaseDocument generateDocs(CaseData caseData, String partyName, Address address, String partyType, String authorisation, boolean isRespondentLiP) {
        boolean isQMEnabled = featureToggleService.isPublicQueryManagementEnabled(caseData);
        NoticeOfDiscontinuanceForm templateData = getNoticeOfDiscontinueData(caseData, partyName, address, isQMEnabled, isRespondentLiP);
        DocmosisTemplates docmosisTemplate = isBilingual(caseData) ? NOTICE_OF_DISCONTINUANCE_BILINGUAL_PDF : NOTICE_OF_DISCONTINUANCE_PDF;
        DocmosisDocument docmosisDocument =
                documentGeneratorService.generateDocmosisDocument(templateData, docmosisTemplate);

        return documentManagementService.uploadDocument(
                authorisation,
                new PDF(
                        getFileName(caseData, docmosisTemplate, partyType),
                        docmosisDocument.getBytes(),
                        DocumentType.NOTICE_OF_DISCONTINUANCE
                )
        );
    }

    private String getFileName(CaseData caseData, DocmosisTemplates docmosisTemplate, String partyType) {
        String partyTypeAndCaseRef =
            featureToggleService.isWelshEnabledForMainCase()
                ? partyType + "_" + caseData.getLegacyCaseReference()
                : caseData.getLegacyCaseReference();
        return String.format(docmosisTemplate.getDocumentTitle(), partyTypeAndCaseRef);
    }

    private NoticeOfDiscontinuanceForm getNoticeOfDiscontinueData(CaseData caseData, String partyName, Address address, boolean isQMEnabled, boolean isRespondentLiP) {
        return new NoticeOfDiscontinuanceForm()
            .setCaseNumber(caseData.getLegacyCaseReference())
            .setClaimReferenceNumber(caseData.getLegacyCaseReference())
            .setLetterIssueDate(LocalDate.now())
            .setDateOfEvent(LocalDate.now())
            .setCoverLetterName(partyName)
            .setAddressLine1(address.getAddressLine1())
            .setAddressLine2(address.getAddressLine2())
            .setAddressLine3(address.getAddressLine3())
            .setPostCode(address.getPostCode())
            .setClaimant1Name(caseData.getApplicant1().getPartyName())
            .setClaimant2Name(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .setDefendant1Name(caseData.getRespondent1().getPartyName())
            .setDefendant2Name(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .setClaimantNum(nonNull(caseData.getApplicant2()) ? "Claimant 1" : "Claimant")
            .setDefendantNum(nonNull(caseData.getRespondent2()) ? "Defendant 1" : "Defendant")
            .setClaimantNumWelsh(nonNull(caseData.getApplicant2()) ? "Hawlydd 1" : "Hawlydd")
            .setDefendantNumWelsh(nonNull(caseData.getRespondent2()) ? "Diffynnydd 1" : "Diffynnydd")
            .setClaimantWhoIsDiscontinue(getClaimantWhoIsDiscontinue(caseData))
            .setClaimantsConsentToDiscontinuance(nonNull(caseData.getClaimantsConsentToDiscontinuance())
                                                     ? getConsentToDiscontinue(caseData) : null)
            .setCourtPermission(nonNull(caseData.getCourtPermissionNeeded())
                                    ? caseData.getCourtPermissionNeeded().getDisplayedValue() : null)
            .setPermissionGranted(nonNull(caseData.getIsPermissionGranted())
                                      ? caseData.getIsPermissionGranted().getDisplayedValue() : null)
            .setJudgeName(isCourtPermissionGranted(caseData)
                              ? caseData.getPermissionGrantedComplex().getPermissionGrantedJudge() : null)
            .setJudgementDate(isCourtPermissionGranted(caseData)
                                  ? caseData.getPermissionGrantedComplex().getPermissionGrantedDate() : null)
            .setJudgementDateWelsh(isCourtPermissionGranted(caseData)
                                       ? formatDateInWelsh(caseData.getPermissionGrantedComplex().getPermissionGrantedDate(), false) : null)
            .setTypeOfDiscontinuance(caseData.getTypeOfDiscontinuance().toString())
            .setTypeOfDiscontinuanceTxt(caseData.getTypeOfDiscontinuance().getType())
            .setPartOfDiscontinuanceTxt(caseData.getPartDiscontinuanceDetails())
            .setDiscontinuingAgainstOneDefendant(getDiscontinueAgainstOneDefendant(caseData))
            .setDiscontinuingAgainstBothDefendants(nonNull(caseData.getIsDiscontinuingAgainstBothDefendants())
                                                       ? caseData.getIsDiscontinuingAgainstBothDefendants().getDisplayedValue() : null)
            .setWelshDate(formatDateInWelsh(LocalDate.now(), false))
            .setQMEnabled(isQMEnabled)
            .setRespondent1LiP(isRespondentLiP);
    }

    private String getClaimantWhoIsDiscontinue(CaseData caseData) {
        return (nonNull(caseData.getClaimantWhoIsDiscontinuing())
                && nonNull(caseData.getClaimantWhoIsDiscontinuing().getValue()))
                ? caseData.getClaimantWhoIsDiscontinuing().getValue().getLabel()
                : caseData.getApplicant1().getPartyName();
    }

    private String getDiscontinueAgainstOneDefendant(CaseData caseData) {
        return (nonNull(caseData.getDiscontinuingAgainstOneDefendant())
                && nonNull(caseData.getDiscontinuingAgainstOneDefendant().getValue()))
                ? caseData.getDiscontinuingAgainstOneDefendant().getValue().getLabel()
                : null;
    }

    private String getConsentToDiscontinue(CaseData caseData) {
        return YesOrNo.YES.equals(
                caseData.getClaimantsConsentToDiscontinuance()) ? "Yes" : "No";
    }

    private boolean isCourtPermissionGranted(CaseData caseData) {
        return nonNull(caseData.getIsPermissionGranted())
                && SettleDiscontinueYesOrNoList.YES.equals(caseData.getIsPermissionGranted());
    }

    private boolean isBilingual(CaseData caseData) {
        return isWelshHearingSelected(caseData) || caseData.isRespondentResponseBilingual();
    }

}
