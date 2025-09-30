package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

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
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.service.AssignCaseToResopondentSolHelper;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.roleassignment.RolesAndAccessAssignmentService;

import java.util.ArrayList;
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
public class AssignCaseToUserCallbackHandler extends CallbackHandler {

    private final AssignCaseToResopondentSolHelper assignCaseToResopondentSolHelper;

    private final ObjectMapper mapper;

    private final GeneralAppFeesService generalAppFeesService;
    private static final List<CaseEvent> EVENTS = List.of(ASSIGN_GA_ROLES);
    public static final String TASK_ID = "AssigningOfRoles";

    private final CoreCaseUserService coreCaseUserService;

    private final CaseDetailsConverter caseDetailsConverter;

    private final GaForLipService gaForLipService;

    private final RolesAndAccessAssignmentService rolesAndAccessAssignmentService;

    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::assignOrgPolicy,
            callbackKey(SUBMITTED), this::assignSolicitorCaseRole
        );
    }

    private CallbackResponse assignOrgPolicy(CallbackParams callbackParams) {
        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String caseId = caseData.getCcdCaseReference().toString();
        log.info("CaseData in AssignCaseToUserCallbackHandler: {}", caseId);
        List<String> errors = new ArrayList<>();

        if (caseData.getCcdState().equals(PENDING_APPLICATION_ISSUED)) {
            GASolicitorDetailsGAspec applicantSolicitor = caseData.getGeneralAppApplnSolicitor();
            caseDataBuilder.applicant1OrganisationPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationID(applicantSolicitor.getOrganisationIdentifier())
                    .build())
                .orgPolicyCaseAssignedRole(APPLICANTSOLICITORONE.getFormattedName()).build());

            if (!gaForLipService.isGaForLip(caseData)) {
                List<Element<GASolicitorDetailsGAspec>> applicantAddlSolList = caseData.getGeneralAppRespondentSolicitors().stream()
                    .filter(userOrgId -> (userOrgId.getValue().getOrganisationIdentifier()
                        .equalsIgnoreCase(caseData.getGeneralAppApplnSolicitor()
                            .getOrganisationIdentifier()))).toList();
                caseDataBuilder.generalAppApplicantAddlSolicitors(applicantAddlSolList);
            }
        }

        if (!gaForLipService.isGaForLip(caseData)) {
            List<Element<GASolicitorDetailsGAspec>> respondentSolicitorsList = caseData.getGeneralAppRespondentSolicitors().stream()
                .filter(userOrgId -> !(userOrgId.getValue().getOrganisationIdentifier()
                    .equalsIgnoreCase(caseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier()))).toList();
            caseDataBuilder.generalAppRespondentSolicitors(respondentSolicitorsList);
        }

        /*
         * Don't assign the case to respondent solicitors if GA is without notice
         * Assign case to Respondent Solicitors only after the payment is made by Applicant.
         * If the Application is Free Application, then assign the respondent roles during Initiation of GA
         * */
        if ((!caseData.getCcdState().equals(PENDING_APPLICATION_ISSUED)
            && ((ofNullable(caseData.getGeneralAppInformOtherParty()).isPresent()
            && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            || (caseData.getGeneralAppRespondentAgreement() != null
            && caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES))))
            || (generalAppFeesService.isFreeApplication(caseData))) {

            if (!gaForLipService.isGaForLip(caseData)) {

                List<Element<GASolicitorDetailsGAspec>> respondentSolicitorsList = caseData.getGeneralAppRespondentSolicitors().stream()
                    .filter(userOrgId -> !(userOrgId.getValue().getOrganisationIdentifier()
                        .equalsIgnoreCase(caseData.getGeneralAppApplnSolicitor().getOrganisationIdentifier()))).toList();

                List<Element<GASolicitorDetailsGAspec>> respondent2SolicitorsList = caseData.getGeneralAppRespondentSolicitors().stream()
                    .filter(userOrgId -> !(userOrgId.getValue().getOrganisationIdentifier()
                        .equalsIgnoreCase(respondentSolicitorsList.get(0).getValue().getOrganisationIdentifier()))).toList();

                caseDataBuilder
                    .respondent1OrganisationPolicy(
                        OrganisationPolicy.builder()
                            .organisation(Organisation.builder()
                                .organisationID(
                                    respondentSolicitorsList.get(0).getValue()
                                        .getOrganisationIdentifier()).build())
                            .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORONE.getFormattedName()).build());

                if (!respondent2SolicitorsList.isEmpty()) {
                    caseDataBuilder
                        .respondent2OrganisationPolicy(
                            OrganisationPolicy.builder()
                                .organisation(Organisation.builder()
                                    .organisationID(respondent2SolicitorsList.get(0)
                                        .getValue().getOrganisationIdentifier())
                                    .build())
                                .orgPolicyCaseAssignedRole(RESPONDENTSOLICITORTWO.getFormattedName()).build());

                }
            } else {
                /* GA for Lip*/
                caseDataBuilder.respondent1OrganisationPolicy(
                    OrganisationPolicy.builder()
                        .orgPolicyCaseAssignedRole(DEFENDANT.getFormattedName()).build());
            }

        }

        rolesAndAccessAssignmentService.copyAllocatedRolesFromRolesAndAccess(caseData.getGeneralAppParentCaseLink().getCaseReference(), caseId);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataBuilder.build().toMap(mapper)).errors(
                errors)
            .build();

    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse assignSolicitorCaseRole(CallbackParams callbackParams) {

        CaseData caseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetails());
        String caseId = caseData.getCcdCaseReference().toString();

        if (caseData.getCcdState().equals(PENDING_APPLICATION_ISSUED)) {
            GASolicitorDetailsGAspec applicantSolicitor = caseData.getGeneralAppApplnSolicitor();
            if (!gaForLipService.isLipApp(caseData)) {
                log.info("Assigning case to Applicant Solicitor One: {} and caseId: {}", applicantSolicitor.getId(), caseId);
                coreCaseUserService.assignCase(caseId, applicantSolicitor.getId(),
                    applicantSolicitor.getOrganisationIdentifier(), APPLICANTSOLICITORONE
                );
                List<Element<GASolicitorDetailsGAspec>> addlApplicantSolList = caseData.getGeneralAppApplicantAddlSolicitors();
                if (Objects.nonNull(addlApplicantSolList) && !addlApplicantSolList.isEmpty()) {
                    for (Element<GASolicitorDetailsGAspec> addlApplicantSolElement : addlApplicantSolList) {
                        log.info("Assigning case to GA Applicant Solicitor One: {} and caseId: {}", addlApplicantSolElement.getValue().getId(), caseId);
                        coreCaseUserService.assignCase(caseId, addlApplicantSolElement.getValue().getId(),
                            addlApplicantSolElement.getValue().getOrganisationIdentifier(),
                            APPLICANTSOLICITORONE
                        );
                    }
                }
            } else {
                log.info("Assigning case to Applicant Solicitor: {} and caseId: {} with no org", applicantSolicitor.getId(), caseId);
                coreCaseUserService.assignCase(caseId, applicantSolicitor.getId(),
                    null, CLAIMANT
                );
            }

        }

        /*
         * Don't assign the case to respondent solicitors if GA is without notice
         * Assign case to Respondent Solicitors only after the payment is made by Applicant.
         * If the Application is Free Application, then assign the respondent roles during Initiation of GA
         * */
        if ((!caseData.getCcdState().equals(PENDING_APPLICATION_ISSUED)
            && ((ofNullable(caseData.getGeneralAppInformOtherParty()).isPresent()
            && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice()))
            || (caseData.getGeneralAppRespondentAgreement() != null
            && caseData.getGeneralAppRespondentAgreement().getHasAgreed().equals(YES))))
            || (generalAppFeesService.isFreeApplication(caseData))) {
            log.info("Assigning case to Respondent Solicitor for caseId: {}", caseId);
            assignCaseToResopondentSolHelper.assignCaseToRespondentSolicitor(caseData, caseId);
        }

        return SubmittedCallbackResponse.builder().build();

    }

}
