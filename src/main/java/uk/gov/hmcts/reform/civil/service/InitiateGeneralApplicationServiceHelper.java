package uk.gov.hmcts.reform.civil.service;

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessDataStoreApi;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRole;
import uk.gov.hmcts.reform.ccd.model.CaseAssignedUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.handler.tasks.BaseExternalTaskHandler.log;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class InitiateGeneralApplicationServiceHelper {

    private final CaseAccessDataStoreApi caseAccessDataStoreApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;


    public boolean isPCClaimantEmailIDSameAsLoginUser(String email, UserDetails userDetails) {

        return StringUtils.isNotBlank(email)
            && userDetails.getEmail().equals(email);
    }

    public boolean isGA_ApplicantSameAsPC_Claimant(CaseData caseData, UserDetails userDetails) {

        return caseData.getApplicantSolicitor1UserDetails() != null
            && caseData.getApplicant1OrganisationPolicy() != null
            && isPCClaimantEmailIDSameAsLoginUser(caseData.getApplicantSolicitor1UserDetails().getEmail(), userDetails);
    }

    public GeneralApplication setApplicantAndRespondentDetailsIfExits(GeneralApplication generalApplication,
                                                                      CaseData caseData, UserDetails userDetails) {

        GeneralApplication.GeneralApplicationBuilder applicationBuilder = generalApplication.toBuilder();

        boolean isGAApplicantSameAsParentCaseClaimant = isGA_ApplicantSameAsPC_Claimant(caseData, userDetails);

        String parentCaseId = caseData.getCcdCaseReference().toString();

        CaseAssignedUserRolesResource userRoles = caseAccessDataStoreApi.getUserRoles(
            getCaaAccessToken(),
            authTokenGenerator.generate(),
            List.of(parentCaseId)
        );

        List<CaseAssignedUserRole> respondentSolicitors = userRoles.getCaseAssignedUserRoles().stream()
            .filter(CA -> !CA.getUserId().equals(userDetails.getId()))
            .collect(Collectors.toList());

        try {
            if (! Collections.isEmpty(respondentSolicitors)) {

                List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

                respondentSolicitors.forEach((RS) -> {

                    GASolicitorDetailsGAspec gaSolicitorDetailsGAspec = GASolicitorDetailsGAspec
                        .builder()
                        .id(RS.getUserId())
                        .email(RS.getCaseRole() != null
                                   ? RS.getCaseRole().equals(caseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole())
                            ? caseData.getApplicantSolicitor1UserDetails().getEmail()
                            : RS.getCaseRole().equals(caseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole())
                            ? caseData.getRespondentSolicitor1EmailAddress()
                            : caseData.getRespondentSolicitor2EmailAddress() :StringUtils.EMPTY )
                        .organisationIdentifier(RS.getCaseRole().equals(caseData.getApplicant1OrganisationPolicy()
                                                                            .getOrgPolicyCaseAssignedRole())
                                                    ? caseData.getApplicant1OrganisationPolicy().getOrganisation()
                            .getOrganisationID() : RS.getCaseRole().equals(caseData.getRespondent1OrganisationPolicy()
                                                                               .getOrgPolicyCaseAssignedRole())
                            ? caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID()
                            : caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID()).build();

                    respondentSols.add(element(gaSolicitorDetailsGAspec));
                });
                applicationBuilder.generalAppRespondentSolictor(respondentSols);
            }

            return applicationBuilder
                .parentClaimantIsApplicant(isGAApplicantSameAsParentCaseClaimant
                                               ? YesOrNo.YES
                                               : YesOrNo.NO).build();

        } catch (Exception e) {
            log.error(e.toString());
            throw e;
        }
    }

    public String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

}
