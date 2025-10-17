package uk.gov.hmcts.reform.civil.service.ga;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INITIATE_GENERAL_APPLICATION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesResource;
import uk.gov.hmcts.reform.civil.config.CrossAccessUserConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GACaseLocation;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;
import uk.gov.hmcts.reform.civil.utils.UserRoleUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class GaInitiateGeneralApplicationHelper {

    private static final List<String> LIP_CASE_ROLES = List.of("[DEFENDANT]", "[CLAIMANT]");

    private final CaseAssignmentApi caseAssignmentApi;
    private final UserRoleCaching userRoleCaching;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserService userService;
    private final CrossAccessUserConfiguration crossAccessUserConfiguration;
    private final FeatureToggleService featureToggleService;

    public boolean respondentAssigned(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null || gaCaseData.getCcdCaseReference() == null) {
            return false;
        }
        String caseId = gaCaseData.getCcdCaseReference().toString();
        log.info("Checking if respondent is assigned to case: {}", caseId);
        CaseAssignmentUserRolesResource userRoles = getUserRoles(caseId);
        List<String> respondentCaseRoles = getRespondentCaseRoles(gaCaseData);
        return isLipCase(gaCaseData, userRoles) || areRespondentRolesAssigned(userRoles, respondentCaseRoles, caseId);
    }

    public boolean isGaApplicantSameAsParentCaseClaimant(GeneralApplicationCaseData gaCaseData, String authToken) {
        if (gaCaseData == null || gaCaseData.getCcdCaseReference() == null) {
            return false;
        }
        String parentCaseId = gaCaseData.getCcdCaseReference().toString();
        List<String> userRolesCaching = userRoleCaching.getUserRoles(authToken, parentCaseId);
        boolean isApplicantSolicitor = UserRoleUtils.isApplicantSolicitor(userRolesCaching);

        if (CollectionUtils.isEmpty(userRolesCaching) || !isApplicantSolicitor
            || gaCaseData.getApplicant1OrganisationPolicy() == null) {
            return false;
        }

        String applicantRole = gaCaseData.getApplicant1OrganisationPolicy().getOrgPolicyCaseAssignedRole();
        if (userRolesCaching.size() == 1) {
            String assignment = userRolesCaching.get(0);
            return assignment != null && assignment.equals(applicantRole);
        }
        return false;
    }

    public boolean hasExplicitLipFlag(GeneralApplicationCaseData gaCaseData) {
        return isYes(gaCaseData.getIsGaApplicantLip())
            || isYes(gaCaseData.getIsGaRespondentOneLip())
            || isYes(gaCaseData.getIsGaRespondentTwoLip());
    }

    public void applyLipFlags(Map<String, Object> map, GeneralApplicationCaseData gaCaseData) {
        applyLipFlag(map, "applicant1Represented", gaCaseData.getIsGaApplicantLip());
        applyLipFlag(map, "respondent1Represented", gaCaseData.getIsGaRespondentOneLip());
        applyLipFlag(map, "specRespondent1Represented", gaCaseData.getIsGaRespondentOneLip());
        applyLipFlag(map, "respondent2Represented", gaCaseData.getIsGaRespondentTwoLip());
        applyLipFlag(map, "specRespondent2Represented", gaCaseData.getIsGaRespondentTwoLip());
    }

    public GeneralApplicationCaseData ensureDefaults(GeneralApplicationCaseData gaCaseData) {
        if (gaCaseData == null) {
            return null;
        }
        var builder = gaCaseData.toBuilder();

        builder.generalAppPBADetails(ofNullable(gaCaseData.getGeneralAppPBADetails())
            .orElseGet(() -> GAPbaDetails.builder().build()));
        builder.generalAppStatementOfTruth(ofNullable(gaCaseData.getGeneralAppStatementOfTruth())
            .orElseGet(() -> GAStatementOfTruth.builder().build()));
        builder.generalAppInformOtherParty(ofNullable(gaCaseData.getGeneralAppInformOtherParty())
            .orElseGet(() -> GAInformOtherParty.builder().build()));
        builder.generalAppUrgencyRequirement(ofNullable(gaCaseData.getGeneralAppUrgencyRequirement())
            .orElseGet(() -> GAUrgencyRequirement.builder().build()));
        builder.generalAppRespondentAgreement(ofNullable(gaCaseData.getGeneralAppRespondentAgreement())
            .orElseGet(() -> GARespondentOrderAgreement.builder().build()));
        builder.generalAppVaryJudgementType(ofNullable(gaCaseData.getGeneralAppVaryJudgementType())
            .orElse(YesOrNo.NO));
        builder.generalAppHearingDetails(ofNullable(gaCaseData.getGeneralAppHearingDetails())
            .orElseGet(() -> GAHearingDetails.builder().build()));
        builder.generalAppHearingDate(ofNullable(gaCaseData.getGeneralAppHearingDate())
            .orElseGet(() -> GAHearingDateGAspec.builder().build()));
        builder.generalAppEvidenceDocument(ofNullable(gaCaseData.getGeneralAppEvidenceDocument())
            .orElseGet(Collections::emptyList));

        builder.applicantPartyName(resolvePartyName(gaCaseData.getApplicantPartyName(), gaCaseData.getApplicant1()));
        builder.claimant1PartyName(resolvePartyName(gaCaseData.getClaimant1PartyName(), gaCaseData.getApplicant1()));
        builder.claimant2PartyName(resolvePartyName(gaCaseData.getClaimant2PartyName(), gaCaseData.getApplicant2()));
        builder.defendant1PartyName(resolvePartyName(gaCaseData.getDefendant1PartyName(), gaCaseData.getRespondent1()));
        builder.defendant2PartyName(resolvePartyName(gaCaseData.getDefendant2PartyName(), gaCaseData.getRespondent2()));

        builder.generalAppSuperClaimType(ofNullable(gaCaseData.getGeneralAppSuperClaimType())
            .orElseGet(() -> ofNullable(gaCaseData.getCaseAccessCategory())
                .map(Enum::name)
                .orElse(null)));

        builder.caseNameHmctsInternal(gaCaseData.getCaseNameHmctsInternal());

        return builder.build();
    }

    private String resolvePartyName(String existing, Party party) {
        if (existing != null) {
            return existing;
        }
        return ofNullable(party).map(Party::getPartyName).orElse(null);
    }

    public List<Element<GeneralApplication>> buildApplications(GeneralApplicationCaseData gaCaseData) {
        List<Element<GeneralApplication>> existing = ofNullable(gaCaseData.getGeneralApplications())
            .orElse(Collections.emptyList());
        if (!existing.isEmpty()) {
            return existing;
        }

        GeneralApplication.GeneralApplicationBuilder appBuilder = GeneralApplication.builder()
            .generalApplicationState(ofNullable(gaCaseData.getState()).map(Enum::name).orElse(null))
            .generalAppType(gaCaseData.getGeneralAppType())
            .generalAppTypeLR(gaCaseData.getGeneralAppTypeLR())
            .generalAppRespondentAgreement(gaCaseData.getGeneralAppRespondentAgreement())
            .businessProcess(BusinessProcess.ready(INITIATE_GENERAL_APPLICATION))
            .generalAppPBADetails(gaCaseData.getGeneralAppPBADetails())
            .generalAppAskForCosts(gaCaseData.getGeneralAppAskForCosts())
            .generalAppDetailsOfOrder(gaCaseData.getGeneralAppDetailsOfOrder())
            .generalAppDetailsOfOrderColl(ofNullable(gaCaseData.getGeneralAppDetailsOfOrderColl())
                .orElse(Collections.emptyList()))
            .generalAppReasonsOfOrder(gaCaseData.getGeneralAppReasonsOfOrder())
            .generalAppReasonsOfOrderColl(ofNullable(gaCaseData.getGeneralAppReasonsOfOrderColl())
                .orElse(Collections.emptyList()))
            .generalAppInformOtherParty(gaCaseData.getGeneralAppInformOtherParty())
            .generalAppConsentOrder(gaCaseData.getGeneralAppConsentOrder())
            .generalAppUrgencyRequirement(gaCaseData.getGeneralAppUrgencyRequirement())
            .generalAppStatementOfTruth(gaCaseData.getGeneralAppStatementOfTruth())
            .generalAppHearingDetails(gaCaseData.getGeneralAppHearingDetails())
            .generalAppApplnSolicitor(gaCaseData.getGeneralAppApplnSolicitor())
            .generalAppRespondentSolicitors(ofNullable(gaCaseData.getGeneralAppRespondentSolicitors())
                .orElse(Collections.emptyList()))
            .generalAppApplicantAddlSolicitors(ofNullable(gaCaseData.getGeneralAppApplicantAddlSolicitors())
                .orElse(Collections.emptyList()))
            .generalAppEvidenceDocument(ofNullable(gaCaseData.getGeneralAppEvidenceDocument())
                .orElse(Collections.emptyList()))
            .generalAppDateDeadline(gaCaseData.getGeneralAppNotificationDeadlineDate())
            .isMultiParty(gaCaseData.getIsMultiParty())
            .isDocumentVisibleGA(gaCaseData.getIsDocumentVisible())
            .parentClaimantIsApplicant(gaCaseData.getParentClaimantIsApplicant())
            .gaApplicantDisplayName(gaCaseData.getApplicantPartyName())
            .generalAppSubmittedDateGAspec(gaCaseData.getSubmittedDate())
            .civilServiceUserRoles(gaCaseData.getCivilServiceUserRoles())
            .applicantPartyName(gaCaseData.getApplicantPartyName())
            .claimant1PartyName(gaCaseData.getClaimant1PartyName())
            .claimant2PartyName(gaCaseData.getClaimant2PartyName())
            .defendant1PartyName(gaCaseData.getDefendant1PartyName())
            .defendant2PartyName(gaCaseData.getDefendant2PartyName())
            .generalAppSuperClaimType(gaCaseData.getGeneralAppSuperClaimType())
            .caseManagementLocation(toCaseLocationCivil(gaCaseData.getCaseManagementLocation()))
            .caseManagementCategory(gaCaseData.getCaseManagementCategory())
            .caseAccessCategory(gaCaseData.getCaseAccessCategory())
            .locationName(gaCaseData.getLocationName())
            .applicationTakenOfflineDate(gaCaseData.getApplicationTakenOfflineDate())
            .generalAppVaryJudgementType(gaCaseData.getGeneralAppVaryJudgementType())
            .generalAppN245FormUpload(gaCaseData.getGeneralAppN245FormUpload())
            .generalAppHearingDate(gaCaseData.getGeneralAppHearingDate())
            .generalAppParentCaseLink(gaCaseData.getGeneralAppParentCaseLink())
            .respondentsResponses(ofNullable(gaCaseData.getRespondentsResponses()).orElse(Collections.emptyList()))
            .isGaApplicantLip(gaCaseData.getIsGaApplicantLip())
            .isGaRespondentOneLip(gaCaseData.getIsGaRespondentOneLip())
            .isGaRespondentTwoLip(gaCaseData.getIsGaRespondentTwoLip())
            .generalAppHelpWithFees(gaCaseData.getGeneralAppHelpWithFees())
            .certOfSC(gaCaseData.getCertOfSC())
            .emailPartyReference(gaCaseData.getEmailPartyReference())
            .mainCaseSubmittedDate(gaCaseData.getSubmittedDate());

        GeneralApplication application = appBuilder.build();
        return List.of(element(application));
    }

    private CaseLocationCivil toCaseLocationCivil(GACaseLocation location) {
        if (location == null) {
            return null;
        }
        return CaseLocationCivil.builder()
            .region(location.getRegion())
            .siteName(location.getSiteName())
            .baseLocation(location.getBaseLocation())
            .address(location.getAddress())
            .postcode(location.getPostcode())
            .build();
    }

    private boolean isLipCase(GeneralApplicationCaseData gaCaseData, CaseAssignmentUserRolesResource userRoles) {
        if (!featureToggleService.isGaForLipsEnabled()) {
            return false;
        }
        if (!hasExplicitLipFlag(gaCaseData) || userRoles.getCaseAssignmentUserRoles() == null) {
            return false;
        }

        boolean multipleAssignments = userRoles.getCaseAssignmentUserRoles().size() > 1;
        boolean containsLipRole = userRoles.getCaseAssignmentUserRoles().stream()
            .map(CaseAssignmentUserRole::getCaseRole)
            .anyMatch(role -> role != null && LIP_CASE_ROLES.contains(role));

        log.info("Checking isLipCase for case: {} -> multipleAssignments={}, containsLipRole={}",
                 gaCaseData.getCcdCaseReference(), multipleAssignments, containsLipRole);
        return multipleAssignments || containsLipRole;
    }

    private boolean areRespondentRolesAssigned(CaseAssignmentUserRolesResource userRoles,
                                               List<String> respondentCaseRoles,
                                               String caseId) {
        log.info("Checking areRespondentRolesAssigned for case: {}", caseId);
        for (String respondentCaseRole : respondentCaseRoles) {
            if (respondentCaseRole == null || userRoles.getCaseAssignmentUserRoles() == null
                || userRoles.getCaseAssignmentUserRoles().stream()
                .noneMatch(a -> a.getCaseRole() != null && respondentCaseRole.equals(a.getCaseRole()))) {
                log.info("Respondent role {} not assigned for case {}", respondentCaseRole, caseId);
                return false;
            }
        }
        return true;
    }

    private List<String> getRespondentCaseRoles(GeneralApplicationCaseData gaCaseData) {
        List<String> roles = new ArrayList<>();
        if (gaCaseData.getRespondent1OrganisationPolicy() != null) {
            roles.add(gaCaseData.getRespondent1OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
        if (YesOrNo.NO.equals(gaCaseData.getRespondent2SameLegalRepresentative())
            && gaCaseData.getRespondent2OrganisationPolicy() != null) {
            roles.add(gaCaseData.getRespondent2OrganisationPolicy().getOrgPolicyCaseAssignedRole());
        }
        return roles;
    }

    private CaseAssignmentUserRolesResource getUserRoles(String caseId) {
        CaseAssignmentUserRolesResource userRoles = caseAssignmentApi.getUserRoles(
            getCaaAccessToken(),
            authTokenGenerator.generate(),
            List.of(caseId)
        );
        log.info("UserRoles from API for GA case {}: {}", caseId, userRoles);
        return userRoles;
    }

    private String getCaaAccessToken() {
        return userService.getAccessToken(
            crossAccessUserConfiguration.getUserName(),
            crossAccessUserConfiguration.getPassword()
        );
    }

    private void applyLipFlag(Map<String, Object> map, String key, YesOrNo lipFlag) {
        if (lipFlag == null) {
            return;
        }
        YesOrNo represented = lipFlag == YesOrNo.YES ? YesOrNo.NO : YesOrNo.YES;
        map.put(key, represented);
    }

    private boolean isYes(YesOrNo value) {
        return YesOrNo.YES == value;
    }
}
