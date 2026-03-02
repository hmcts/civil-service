package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.ga.utils.OrgPolicyUtils.getRespondent1SolicitorOrgId;
import static uk.gov.hmcts.reform.civil.ga.utils.OrgPolicyUtils.getRespondent2SolicitorOrgId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorEmailValidation {

    private final GaForLipService gaForLipService;

    private GASolicitorDetailsGAspec updateSolDetails(String updateEmail,
                                                      GASolicitorDetailsGAspec generalAppSolicitor) {

        GASolicitorDetailsGAspec.GASolicitorDetailsGAspecBuilder gaSolicitorDetailsGAspecBuilder =
            generalAppSolicitor.toBuilder();

        gaSolicitorDetailsGAspecBuilder.email(updateEmail);

        return gaSolicitorDetailsGAspecBuilder.build();

    }

    private boolean checkIfOrgIdAndEmailAreSame(String organisationID, String civilSolEmail,
                                               GASolicitorDetailsGAspec generalAppSolicitor) {

        return organisationID.equals(generalAppSolicitor.getOrganisationIdentifier())
            && ! generalAppSolicitor.getEmail().equals(civilSolEmail);

    }

    private GASolicitorDetailsGAspec checkIfOrgIDMatch(GASolicitorDetailsGAspec generalAppSolicitor,
                                                       GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData gaCaseData) {

        // civil claim applicant
        if (civilCaseData.getApplicant1OrganisationPolicy() != null
            && checkIfOrgIdExists(civilCaseData.getApplicant1OrganisationPolicy())
            && (checkIfOrgIdAndEmailAreSame(civilCaseData.getApplicant1OrganisationPolicy()
                                                .getOrganisation().getOrganisationID(),
                                            civilCaseData.getApplicantSolicitor1UserDetails().getEmail(),
                                            generalAppSolicitor))) {

            // Update GA Solicitor Email ID as same as Civil Claim claimant Solicitor Email
            log.info("Update GA Solicitor Email ID as same as Civil Claim claimant Solicitor Email");
            return updateSolDetails(civilCaseData.getApplicantSolicitor1UserDetails()
                                            .getEmail(), generalAppSolicitor);

        }

        // civil claim defendant 1
        if (civilCaseData.getRespondent1OrganisationPolicy() != null
            && getRespondent1SolicitorOrgId(civilCaseData) != null
            && (checkIfOrgIdAndEmailAreSame(getRespondent1SolicitorOrgId(civilCaseData),
                                            civilCaseData.getRespondentSolicitor1EmailAddress(),
                                            generalAppSolicitor))) {

            log.info("Update GA Solicitor Email ID as same as Civil Claim Respondent Solicitor one Email");
            return updateSolDetails(civilCaseData.getRespondentSolicitor1EmailAddress(), generalAppSolicitor);

        }

        // civil claim defendant 2
        if (YES.equals(gaCaseData.getIsMultiParty())
            && NO.equals(civilCaseData.getRespondent2SameLegalRepresentative())
            && (getRespondent2SolicitorOrgId(civilCaseData) != null
            && (checkIfOrgIdAndEmailAreSame(getRespondent2SolicitorOrgId(civilCaseData),
                                            civilCaseData.getRespondentSolicitor2EmailAddress(),
                                            generalAppSolicitor)))) {

            log.info("Update GA Solicitor Email ID as same as Civil Claim Respondent Solicitor Two Email");
            return updateSolDetails(civilCaseData.getRespondentSolicitor2EmailAddress(), generalAppSolicitor);
        }
        return generalAppSolicitor;

    }

    private boolean checkIfOrgIdExists(OrganisationPolicy organisationPolicy) {
        return organisationPolicy.getOrganisation() != null
            && organisationPolicy.getOrganisation().getOrganisationID() != null;
    }

    public GeneralApplicationCaseData validateSolicitorEmail(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData gaCaseData) {

        // GA Applicant solicitor
        GeneralApplicationCaseData caseDataBuilder = gaCaseData.copy();
        if (!gaForLipService.isGaForLip(gaCaseData)) {
            caseDataBuilder.generalAppApplnSolicitor(checkIfOrgIDMatch(gaCaseData.getGeneralAppApplnSolicitor(),
                                                                   civilCaseData, gaCaseData));

            // GA Respondent solicitor
            List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = newArrayList();
            gaCaseData.getGeneralAppRespondentSolicitors().forEach(rs -> generalAppRespondentSolicitors
                .add(element(checkIfOrgIDMatch(rs.getValue(), civilCaseData, gaCaseData))));

            caseDataBuilder.generalAppRespondentSolicitors(generalAppRespondentSolicitors.isEmpty()
                                                               ? gaCaseData.getGeneralAppRespondentSolicitors()
                                                               : generalAppRespondentSolicitors);
        } else {
            validateLipEmail(civilCaseData, gaCaseData, caseDataBuilder);
        }

        return caseDataBuilder.build();
    }

    private void validateLipEmail(GeneralApplicationCaseData civilCaseData, GeneralApplicationCaseData gaCaseData,
                                  GeneralApplicationCaseData caseDataBuilder) {

        String applicant1Email = gaForLipService.getApplicant1Email(civilCaseData);
        String defendant1Email = gaForLipService.getDefendant1Email(civilCaseData);

        if (gaForLipService.isLipApp(gaCaseData)) {
            if (gaCaseData.getParentClaimantIsApplicant().equals(YES)) {
                checkApplicantLip(gaCaseData, caseDataBuilder, applicant1Email);
            } else {
                checkApplicantLip(gaCaseData, caseDataBuilder, defendant1Email);
            }
        }
        if (gaForLipService.isLipResp(gaCaseData)) {
            if (gaCaseData.getParentClaimantIsApplicant().equals(YES)) {
                checkRespondentsLip(gaCaseData, caseDataBuilder, defendant1Email);
            } else {
                checkRespondentsLip(gaCaseData, caseDataBuilder, applicant1Email);
            }
        }
    }

    private void checkApplicantLip(GeneralApplicationCaseData gaCaseData,
                                   GeneralApplicationCaseData caseDataBuilder,
                                   String userEmail) {
        if (userEmail != null
            && gaCaseData.getGeneralAppApplnSolicitor() != null
            && gaCaseData.getGeneralAppApplnSolicitor().getEmail() != null
            && !userEmail.equals(gaCaseData.getGeneralAppApplnSolicitor().getEmail())) {
            caseDataBuilder.generalAppApplnSolicitor(updateSolDetails(
                userEmail, gaCaseData.getGeneralAppApplnSolicitor()));
        }
    }

    private void checkRespondentsLip(GeneralApplicationCaseData gaCaseData,
                                     GeneralApplicationCaseData caseDataBuilder,
                                     String userEmail) {
        List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = newArrayList();
        /*GA for Lip is 1v1*/
        if (userEmail != null
            && gaCaseData.getGeneralAppRespondentSolicitors() != null
            && !gaCaseData.getGeneralAppRespondentSolicitors().isEmpty()
            && !userEmail.equals(gaCaseData.getGeneralAppRespondentSolicitors().get(0).getValue().getEmail())) {

            generalAppRespondentSolicitors.add(element(updateSolDetails(
                userEmail, gaCaseData.getGeneralAppRespondentSolicitors().get(0).getValue())));

            caseDataBuilder.generalAppRespondentSolicitors(generalAppRespondentSolicitors);
        }
    }
}
