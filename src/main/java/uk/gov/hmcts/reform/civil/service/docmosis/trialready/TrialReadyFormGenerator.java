package uk.gov.hmcts.reform.civil.service.docmosis.trialready;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseprogression.HearingOtherComments;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.trialready.TrialReadyForm;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

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
    private final AssignCategoryId assignCategoryId;

    private static final String TASK_ID_APPLICANT = "GenerateTrialReadyFormApplicant";
    private static final String TASK_ID_RESPONDENT1 = "GenerateTrialReadyFormRespondent1";

    public CaseDocument generate(CaseData caseData, String authorisation, String camundaActivity, CaseRole userRole) {
        TrialReadyForm templateData = getTemplateData(caseData, camundaActivity);

        DocmosisTemplates template = TRIAL_READY;
        DocmosisDocument document = documentGeneratorService.generateDocmosisDocument(templateData, template);
        CaseDocument trialReadyDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, template, camundaActivity),
                document.getBytes(),
                DocumentType.TRIAL_READY_DOCUMENT
            )
        );

        switch (userRole) {
            case APPLICANTSOLICITORONE, CLAIMANT:
                assignCategoryId.assignCategoryIdToCaseDocument(trialReadyDocument, DocCategory.DQ_APP1.getValue());
                break;
            case RESPONDENTSOLICITORONE, DEFENDANT:
                assignCategoryId.assignCategoryIdToCaseDocument(trialReadyDocument, DocCategory.DQ_DEF1.getValue());
                break;
            case RESPONDENTSOLICITORTWO:
                assignCategoryId.assignCategoryIdToCaseDocument(trialReadyDocument, DocCategory.DQ_DEF2.getValue());
                break;
            default:
        }

        return trialReadyDocument.toBuilder().ownedBy(userRole).build();
    }

    private TrialReadyForm getTemplateData(CaseData caseData, String camundaActivity) {
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

        return completeTrialReadyFormWithOptionalFields(caseData, trialReadyForm, camundaActivity).build();
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template, String camundaActivity) {
        return String.format(
            template.getDocumentTitle(),
            getUserLastName(caseData, camundaActivity),
            formatLocalDate(LocalDate.now(), DATE));
    }

    private String getUserLastName(CaseData caseData, String camundaActivity) {
        if (TASK_ID_APPLICANT.equals(camundaActivity)) {
            log.info("Generating document for Applicant");
            return getTypeUserLastName(caseData.getApplicant1());
        } else if (TASK_ID_RESPONDENT1.equals(camundaActivity)) {
            log.info("Generating document for Respondent 1");
            return getTypeUserLastName(caseData.getRespondent1());
        } else {
            log.info("Generating document for Respondent 2");
            return getTypeUserLastName(caseData.getRespondent2());
        }
    }

    private String getTypeUserLastName(Party party) {
        return switch (party.getType()) {
            case INDIVIDUAL -> party.getIndividualLastName();
            case COMPANY -> party.getCompanyName();
            case SOLE_TRADER -> party.getSoleTraderLastName();
            default -> party.getOrganisationName();
        };
    }

    private TrialReadyForm.TrialReadyFormBuilder completeTrialReadyFormWithOptionalFields(
        CaseData caseData, TrialReadyForm.TrialReadyFormBuilder trialReadyForm, String camundaActivity) {
        if (TASK_ID_APPLICANT.equals(camundaActivity)) {
            return addUserFields(caseData.getTrialReadyApplicant(),
                                 caseData.getApplicantRevisedHearingRequirements(),
                                 caseData.getApplicantHearingOtherComments(), trialReadyForm);
        } else if (TASK_ID_RESPONDENT1.equals(camundaActivity)) {
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
            .hearingRequirementsText(YesOrNo.YES.equals(
                hearingRequirements
                    .getRevisedHearingRequirements()) ? hearingRequirements.getRevisedHearingComments() : null)
            .additionalInfo(nonNull(hearingOtherComments) ? hearingOtherComments.getHearingOtherComments() : null);

    }

    private boolean checkReference(CaseData caseData) {
        return nonNull(caseData.getSolicitorReferences());
    }
}
