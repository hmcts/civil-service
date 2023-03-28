package uk.gov.hmcts.reform.civil.service.docmosis.trialready;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseprogression.HearingOtherComments;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.trialready.TrialReadyForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.TRIAL_READY;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrialReadyFormGenerator {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    public CaseDocument generate(CaseData caseData, String authorisation) {
        TrialReadyForm templateData = getTemplateData(caseData);

        DocmosisTemplates template = TRIAL_READY;
        DocmosisDocument document = documentGeneratorService.generateDocmosisDocument(templateData, template);
        return documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, template),
                document.getBytes(),
                DocumentType.TRIAL_READY_DOCUMENT
            )
        );
    }

    private TrialReadyForm getTemplateData(CaseData caseData) {
        var trialReadyForm = TrialReadyForm.builder()
            .caseNumber(caseData.getLegacyCaseReference())
            .date(formatLocalDate(LocalDate.now(), DATE))
            .claimant1(caseData.getApplicant1().getPartyName())
            .isClaimant2(nonNull(caseData.getApplicant2()))
            .claimant2(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1(caseData.getRespondent1().getPartyName())
            .isDefendant2(nonNull(caseData.getRespondent2()))
            .defendant2(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .claimantReferenceNumber(checkReference(caseData)
                                         ? caseData.getSolicitorReferences().getApplicantSolicitor1Reference() : null)
            .defendantRefNumber(checkReference(caseData)
                                    ? caseData.getSolicitorReferences().getRespondentSolicitor1Reference() : null)
            .isDefendant2RefDiff(checkReference(caseData)
                                     && nonNull(caseData.getSolicitorReferences().getRespondentSolicitor2Reference()))
            .defendant2RefNumber(checkReference(caseData)
                                     ? caseData.getSolicitorReferences().getRespondentSolicitor2Reference() : null);

        return completeTrialReadyFormWithOptionalFields(caseData, trialReadyForm).build();
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template) {
        return String.format(
            template.getDocumentTitle(),
            getUserLastName(caseData),
            formatLocalDate(LocalDate.now(), DATE));
    }

    private String getUserLastName(CaseData caseData) {
        if (YesOrNo.YES.equals(caseData.getIsApplicant1())) {
            log.info("Generating document for Applicant");
            return getTypeUserLastName(caseData.getApplicant1());
        } else if (YesOrNo.YES.equals(caseData.getIsRespondent1())) {
            log.info("Generating document for Respondent 1");
            return getTypeUserLastName(caseData.getRespondent1());
        } else {
            log.info("Generating document for Respondent 2");
            return getTypeUserLastName(caseData.getRespondent2());
        }
    }

    private String getTypeUserLastName(Party party) {
        switch (party.getType()) {
            case INDIVIDUAL:
                return party.getIndividualLastName();
            case COMPANY:
                return party.getCompanyName();
            case SOLE_TRADER:
                return party.getSoleTraderLastName();
            default:
                return party.getOrganisationName();
        }
    }

    private TrialReadyForm.TrialReadyFormBuilder completeTrialReadyFormWithOptionalFields(
        CaseData caseData, TrialReadyForm.TrialReadyFormBuilder trialReadyForm) {
        if (YesOrNo.YES.equals(caseData.getIsApplicant1())) {
            return addUserFields(caseData.getTrialReadyApplicant(),
                                 caseData.getApplicantRevisedHearingRequirements(),
                                 caseData.getApplicantHearingOtherComments(), trialReadyForm);
        } else if (YesOrNo.YES.equals(caseData.getIsRespondent1())) {
            return addUserFields(caseData.getTrialReadyRespondent1(),
                                 caseData.getRespondent1RevisedHearingRequirements(),
                                 caseData.getRespondent1HearingOtherComments(), trialReadyForm);
        } else {
            return addUserFields(caseData.getTrialReadyRespondent2(),
                                 caseData.getRespondent2RevisedHearingRequirements(),
                                 caseData.getRespondent2HearingOtherComments(), trialReadyForm);
        }
    }

    private TrialReadyForm.TrialReadyFormBuilder addUserFields(YesOrNo trialReadyCheck,
                                                               RevisedHearingRequirements hearingRequirements,
                                                               HearingOtherComments hearingOtherComments,
                                                               TrialReadyForm.TrialReadyFormBuilder trialReadyForm) {
        return trialReadyForm.trialReadyAccepted(trialReadyCheck.equals(YesOrNo.YES))
            .trialReadyDeclined(trialReadyCheck.equals(YesOrNo.NO))
            .hearingRequirementsCheck(YesOrNo.YES.equals(
                hearingRequirements.getRevisedHearingRequirements()) ? "Yes" : "No")
            .hearingRequirementsText(hearingRequirements.getRevisedHearingComments())
            .additionalInfo(nonNull(hearingOtherComments) ? hearingOtherComments.getHearingOtherComments() : null);

    }

    private boolean checkReference(CaseData caseData) {
        return nonNull(caseData.getSolicitorReferences());
    }
}
