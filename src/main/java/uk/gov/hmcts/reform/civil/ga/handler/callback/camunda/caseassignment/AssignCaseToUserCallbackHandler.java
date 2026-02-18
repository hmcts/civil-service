package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.ga.service.AssignCaseToRespondentSolHelper;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.ga.service.roleassignment.RolesAndAccessAssignmentService;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_GA_ROLES;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignCaseToUserCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private final AssignCaseToRespondentSolHelper assignCaseToRespondentSolHelper;

    private final ObjectMapper mapper;

    private final GeneralAppFeesService generalAppFeesService;
    private static final List<CaseEvent> EVENTS = List.of(ASSIGN_GA_ROLES);
    public static final String TASK_ID = "AssigningOfRoles";

    private final CoreCaseUserService coreCaseUserService;

    private final CaseDetailsConverter caseDetailsConverter;

    private final GaForLipService gaForLipService;

    private final RolesAndAccessAssignmentService rolesAndAccessAssignmentService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::assignOrgPolicy,
            callbackKey(SUBMITTED), this::assignSolicitorCaseRole
        );
    }

    private CallbackResponse assignOrgPolicy(CallbackParams callbackParams) {
        var caseData = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());
        var caseDataBuilder = caseData.toBuilder();
        var caseId = caseData.getCcdCaseReference().toString();
        log.info("Assigning OrgPolicy for caseId: {}", caseId);

        if (PENDING_APPLICATION_ISSUED.equals(caseData.getCcdState())) {
            assignApplicantOrgPolicy(caseData, caseDataBuilder);
        }

        assignRespondentOrgPolicy(caseData, caseDataBuilder);

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(
            caseData.getGeneralAppParentCaseLink().getCaseReference(), caseId);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(mapper))
            .build();
    }

    private void assignApplicantOrgPolicy(GeneralApplicationCaseData caseData,
                                          GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> builder) {
        var applicantSolicitor = caseData.getGeneralAppApplnSolicitor();
        builder.applicant1OrganisationPolicy(
            buildOrganisationPolicy(applicantSolicitor, APPLICANTSOLICITORONE.getFormattedName()));

        if (!gaForLipService.isGaForLip(caseData)) {
            var applicantOrgId = applicantSolicitor.getOrganisationIdentifier();
            var applicantAddlSolList = caseData.getGeneralAppRespondentSolicitors().stream()
                .filter(sol -> sol.getValue().getOrganisationIdentifier().equalsIgnoreCase(applicantOrgId))
                .toList();
            builder.generalAppApplicantAddlSolicitors(applicantAddlSolList);
        }
    }

    private void assignRespondentOrgPolicy(GeneralApplicationCaseData caseData,
                                           GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> builder) {
        if (gaForLipService.isGaForLip(caseData)) {
            assignRespondentLipOrgPolicy(caseData, builder);
            return;
        }

        var applicantOrgId = caseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier();
        var respondentSolicitorsList = caseData.getGeneralAppRespondentSolicitors().stream()
            .filter(sol -> !sol.getValue().getOrganisationIdentifier().equalsIgnoreCase(applicantOrgId))
            .toList();

        builder.generalAppRespondentSolicitors(respondentSolicitorsList);

        if (shouldAssignRespondentSolicitorRoles(caseData)) {
            assignRespondentSolicitorRoles(respondentSolicitorsList, builder);
        }
    }

    private void assignRespondentLipOrgPolicy(GeneralApplicationCaseData caseData,
                                              GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> builder) {
        if (shouldAssignRespondentSolicitorRoles(caseData)) {
            builder.respondent1OrganisationPolicy(
                new OrganisationPolicy().setOrgPolicyCaseAssignedRole(DEFENDANT.getFormattedName()));
        }
    }

    private boolean shouldAssignRespondentSolicitorRoles(GeneralApplicationCaseData caseData) {
        boolean isNotPending = !PENDING_APPLICATION_ISSUED.equals(caseData.getCcdState());
        boolean hasNoticeOrAgreement = (ofNullable(caseData.getGeneralAppInformOtherParty()).isPresent()
            && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            || (caseData.getGeneralAppRespondentAgreement() != null
            && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed()));

        return (isNotPending && hasNoticeOrAgreement) || generalAppFeesService.isFreeApplication(caseData);
    }

    private void assignRespondentSolicitorRoles(List<Element<GASolicitorDetailsGAspec>> respondentSolicitorsList,
                                                GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> builder) {
        if (respondentSolicitorsList.isEmpty()) {
            return;
        }

        builder.respondent1OrganisationPolicy(
            buildOrganisationPolicy(respondentSolicitorsList.get(0).getValue(),
                                    RESPONDENTSOLICITORONE.getFormattedName()));

        if (respondentSolicitorsList.size() > 1) {
            builder.respondent2OrganisationPolicy(
                buildOrganisationPolicy(respondentSolicitorsList.get(1).getValue(),
                                        RESPONDENTSOLICITORTWO.getFormattedName()));
        }
    }

    private CallbackResponse assignSolicitorCaseRole(CallbackParams callbackParams) {
        var caseData = caseDetailsConverter.toGeneralApplicationCaseData(callbackParams.getRequest().getCaseDetails());
        var caseId = caseData.getCcdCaseReference().toString();

        if (PENDING_APPLICATION_ISSUED.equals(caseData.getCcdState())) {
            assignApplicantCaseRoles(caseData, caseId);
        }

        if (shouldAssignRespondentSolicitorRoles(caseData)) {
            log.info("Assigning case to Respondent Solicitor for caseId: {}", caseId);
            assignCaseToRespondentSolHelper.assignCaseToRespondentSolicitor(caseData, caseId);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void assignApplicantCaseRoles(GeneralApplicationCaseData caseData, String caseId) {
        var applicantSolicitor = caseData.getGeneralAppApplnSolicitor();

        if (gaForLipService.isLipApp(caseData)) {
            log.info("Assigning case to Applicant Solicitor: {} and caseId: {} with no org", applicantSolicitor.getId(), caseId);
            coreCaseUserService.assignCase(caseId, applicantSolicitor.getId(), null, CLAIMANT);
        } else {
            log.info("Assigning case to Applicant Solicitor One: {} and caseId: {}", applicantSolicitor.getId(), caseId);
            coreCaseUserService.assignCase(caseId, applicantSolicitor.getId(),
                                           applicantSolicitor.getOrganisationIdentifier(), APPLICANTSOLICITORONE
            );
            assignAdditionalApplicantSolicitors(caseData, caseId);
        }
    }

    private void assignAdditionalApplicantSolicitors(GeneralApplicationCaseData caseData, String caseId) {
        var addlApplicantSolList = caseData.getGeneralAppApplicantAddlSolicitors();
        if (addlApplicantSolList != null && !addlApplicantSolList.isEmpty()) {
            for (var addlApplicantSolElement : addlApplicantSolList) {
                log.info("Assigning case to GA Applicant Solicitor One: {} and caseId: {}", addlApplicantSolElement.getValue().getId(), caseId);
                coreCaseUserService.assignCase(caseId, addlApplicantSolElement.getValue().getId(),
                                               addlApplicantSolElement.getValue().getOrganisationIdentifier(),
                                               APPLICANTSOLICITORONE
                );
            }
        }
    }

    private OrganisationPolicy buildOrganisationPolicy(GASolicitorDetailsGAspec solicitor, String role) {
        return new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID(solicitor.getOrganisationIdentifier()))
            .setOrgPolicyCaseAssignedRole(role);
    }

}
