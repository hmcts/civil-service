package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getRespondent1SolicitorOrgId;
import static uk.gov.hmcts.reform.civil.utils.OrgPolicyUtils.getRespondent2SolicitorOrgId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolicitorEmailValidation {

    private final GaForLipService gaForLipService;

    private GASolicitorDetailsGAspec updateSolDetails(String updateEmail,
                                                      GASolicitorDetailsGAspec generalAppSolicitor) {
        return generalAppSolicitor.toBuilder()
            .email(updateEmail)
            .build();
    }

    private boolean checkIfOrgIdAndEmailAreSame(String organisationID, String civilSolEmail,
                                                GASolicitorDetailsGAspec generalAppSolicitor) {
        return organisationID.equals(generalAppSolicitor.getOrganisationIdentifier())
            && !generalAppSolicitor.getEmail().equals(civilSolEmail);
    }

    private GASolicitorDetailsGAspec checkIfOrgIDMatch(GASolicitorDetailsGAspec generalAppSolicitor,
                                                       CaseData civilCaseData,
                                                       GeneralApplicationCaseData gaCaseData) {
        if (generalAppSolicitor == null) {
            return null;
        }

        // civil claim applicant
        if (civilCaseData.getApplicant1OrganisationPolicy() != null
            && checkIfOrgIdExists(civilCaseData.getApplicant1OrganisationPolicy())
            && checkIfOrgIdAndEmailAreSame(
                civilCaseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID(),
                civilCaseData.getApplicantSolicitor1UserDetails().getEmail(),
                generalAppSolicitor
            )) {
            log.info("Update GA Solicitor Email ID as same as Civil Claim claimant Solicitor Email");
            return updateSolDetails(
                civilCaseData.getApplicantSolicitor1UserDetails().getEmail(),
                generalAppSolicitor
            );
        }

        // civil claim defendant 1
        if (civilCaseData.getRespondent1OrganisationPolicy() != null
            && getRespondent1SolicitorOrgId(civilCaseData) != null
            && checkIfOrgIdAndEmailAreSame(
                getRespondent1SolicitorOrgId(civilCaseData),
                civilCaseData.getRespondentSolicitor1EmailAddress(),
                generalAppSolicitor
            )) {
            log.info("Update GA Solicitor Email ID as same as Civil Claim Respondent Solicitor One Email");
            return updateSolDetails(
                civilCaseData.getRespondentSolicitor1EmailAddress(),
                generalAppSolicitor
            );
        }

        // civil claim defendant 2
        if (YES.equals(gaCaseData.getIsMultiParty())
            && NO.equals(civilCaseData.getRespondent2SameLegalRepresentative())
            && getRespondent2SolicitorOrgId(civilCaseData) != null
            && checkIfOrgIdAndEmailAreSame(
                getRespondent2SolicitorOrgId(civilCaseData),
                civilCaseData.getRespondentSolicitor2EmailAddress(),
                generalAppSolicitor
            )) {
            log.info("Update GA Solicitor Email ID as same as Civil Claim Respondent Solicitor Two Email");
            return updateSolDetails(
                civilCaseData.getRespondentSolicitor2EmailAddress(),
                generalAppSolicitor
            );
        }

        return generalAppSolicitor;
    }

    private boolean checkIfOrgIdExists(OrganisationPolicy organisationPolicy) {
        return organisationPolicy.getOrganisation() != null
            && organisationPolicy.getOrganisation().getOrganisationID() != null;
    }

    public GeneralApplicationCaseData validateSolicitorEmail(CaseData civilCaseData,
                                                             GeneralApplicationCaseData gaCaseData) {

        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder = gaCaseData.toBuilder();
        if (!gaForLipService.isGaForLip(gaCaseData)) {
            GASolicitorDetailsGAspec updatedApplicant = checkIfOrgIDMatch(
                gaCaseData.getGeneralAppApplnSolicitor(), civilCaseData, gaCaseData);
            builder.generalAppApplnSolicitor(updatedApplicant);

            List<Element<GASolicitorDetailsGAspec>> updatedRespondents = newArrayList();
            if (gaCaseData.getGeneralAppRespondentSolicitors() != null) {
                gaCaseData.getGeneralAppRespondentSolicitors()
                    .forEach(rs -> {
                        GASolicitorDetailsGAspec updated = checkIfOrgIDMatch(
                            rs.getValue(), civilCaseData, gaCaseData);
                        if (updated != null) {
                            updatedRespondents.add(element(updated));
                        }
                    });
            }

            builder.generalAppRespondentSolicitors(updatedRespondents.isEmpty()
                                                      ? gaCaseData.getGeneralAppRespondentSolicitors()
                                                      : updatedRespondents);
        } else {
            validateLipEmail(civilCaseData, gaCaseData, builder);
        }

        return builder.build();
    }

    private void validateLipEmail(CaseData civilCaseData,
                                  GeneralApplicationCaseData gaCaseData,
                                  GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder) {

        String applicant1Email = gaForLipService.getApplicant1Email(civilCaseData);
        String defendant1Email = gaForLipService.getDefendant1Email(civilCaseData);

        if (gaForLipService.isLipAppGa(gaCaseData)) {
            if (YES.equals(gaCaseData.getParentClaimantIsApplicant())) {
                checkApplicantLip(gaCaseData, builder, applicant1Email);
            } else {
                checkApplicantLip(gaCaseData, builder, defendant1Email);
            }
        }
        if (gaForLipService.isLipRespGa(gaCaseData)) {
            if (YES.equals(gaCaseData.getParentClaimantIsApplicant())) {
                checkRespondentsLip(gaCaseData, builder, defendant1Email);
            } else {
                checkRespondentsLip(gaCaseData, builder, applicant1Email);
            }
        }
    }

    private void checkApplicantLip(GeneralApplicationCaseData gaCaseData,
                                   GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder,
                                   String userEmail) {
        if (userEmail != null
            && gaCaseData.getGeneralAppApplnSolicitor() != null
            && gaCaseData.getGeneralAppApplnSolicitor().getEmail() != null
            && !userEmail.equals(gaCaseData.getGeneralAppApplnSolicitor().getEmail())) {
            builder.generalAppApplnSolicitor(updateSolDetails(
                userEmail, gaCaseData.getGeneralAppApplnSolicitor()));
        }
    }

    private void checkRespondentsLip(GeneralApplicationCaseData gaCaseData,
                                     GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder,
                                     String userEmail) {
        if (userEmail == null
            || gaCaseData.getGeneralAppRespondentSolicitors() == null
            || gaCaseData.getGeneralAppRespondentSolicitors().isEmpty()) {
            return;
        }

        GASolicitorDetailsGAspec respondentSolicitor =
            gaCaseData.getGeneralAppRespondentSolicitors().get(0).getValue();
        if (respondentSolicitor != null
            && !Objects.equals(userEmail, respondentSolicitor.getEmail())) {
            List<Element<GASolicitorDetailsGAspec>> updatedRespondents = newArrayList();
            updatedRespondents.add(element(updateSolDetails(userEmail, respondentSolicitor)));
            builder.generalAppRespondentSolicitors(updatedRespondents);
        }
    }
}
