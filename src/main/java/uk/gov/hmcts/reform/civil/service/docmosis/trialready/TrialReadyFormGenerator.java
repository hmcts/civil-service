package uk.gov.hmcts.reform.civil.service.docmosis.trialready;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.docmosis.trialready.TrialReadyForm;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.documents.PDF;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.docmosis.TemplateDataGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;

import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.TRIAL_READY;

@Service
@RequiredArgsConstructor
public class TrialReadyFormGenerator implements TemplateDataGenerator<TrialReadyForm> {

    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public CaseDocument generate(CaseData caseData, String authorisation) {

        CaseDocument caseDocument;
        TrialReadyForm templateData = getTemplateData(caseData);
        DocmosisTemplates template = TRIAL_READY;

        DocmosisDocument document =
            documentGeneratorService.generateDocmosisDocument(templateData, template);
        caseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                getFileName(caseData, template, authorisation),
                document.getBytes(),
                DocumentType.TRIAL_READY_DOCUMENT
            )
        );
        return caseDocument;
    }

    @Override
    public TrialReadyForm getTemplateData(CaseData caseData) {
        return TrialReadyForm.builder()
            .claimant1(caseData.getApplicant1().getPartyName())
            .isClaimant2(nonNull(caseData.getApplicant2()))
            .claimant2(nonNull(caseData.getApplicant2()) ? caseData.getApplicant2().getPartyName() : null)
            .defendant1(caseData.getRespondent1().getPartyName())
            .isDefendant2(nonNull(caseData.getRespondent2()))
            .defendant2(nonNull(caseData.getRespondent2()) ? caseData.getRespondent2().getPartyName() : null)
            .claimantReferenceNumber(caseData.getSolicitorReferences().getApplicantSolicitor1Reference())
            .defendantRefNumber(caseData.getSolicitorReferences().getRespondentSolicitor1Reference())
            .isDefendant2RefDiff(nonNull(caseData.getSolicitorReferences().getRespondentSolicitor2Reference()))
            .defendant2RefNumber(caseData.getSolicitorReferences().getRespondentSolicitor2Reference())
            .trialReadyAccepted(true) //modify with CIV-2732 changes
            .trialReadyDeclined(false)
            .hearingRequirementsCheck("Yes")
            .hearingRequirementsText("Hearing Requirements Test text")
            .additionalInfo("Additional Info Test text")
            .build();
    }

    private String getFileName(CaseData caseData, DocmosisTemplates template, String authorisation) {
        return String.format(
            template.getDocumentTitle(),
            getUserLastName(caseData, authorisation),
            formatLocalDate(LocalDate.now(), DATE));
    }

    private String getUserLastName(CaseData caseData, String authorisation) {
        if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(),
                                               userService.getUserInfo(authorisation).getUid(),
                                               CaseRole.APPLICANTSOLICITORONE)) {
            return getTypeUserLastName(caseData.getApplicant1());
        } else if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(),
                                                       userService.getUserInfo(authorisation).getUid(),
                                                       CaseRole.RESPONDENTSOLICITORONE)) {
            return getTypeUserLastName(caseData.getRespondent1());
        } else if (coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(),
                                                       userService.getUserInfo(authorisation).getUid(),
                                                       CaseRole.RESPONDENTSOLICITORTWO)) {
            return getTypeUserLastName(caseData.getRespondent2());
        } else {
            throw new IllegalArgumentException("Invalid user type");
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
            case ORGANISATION:
                return party.getOrganisationName();
            default:
                throw new IllegalArgumentException("Invalid user type");
        }
    }
}
