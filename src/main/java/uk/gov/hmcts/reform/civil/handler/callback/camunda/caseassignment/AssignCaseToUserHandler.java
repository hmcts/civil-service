package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.prd.model.ProfessionalUsersResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_CASE_TO_APPLICANT_SOLICITOR1;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class AssignCaseToUserHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ASSIGN_CASE_TO_APPLICANT_SOLICITOR1);
    public static final String TASK_ID = "CaseAssignmentToApplicantSolicitor1";
    private static final String CASEWORKER_CAA_ROLE = "pui-caa";

    private final CoreCaseUserService coreCaseUserService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ObjectMapper objectMapper;
    private final OrganisationService organisationService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::assignSolicitorCaseRole
        );
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
        IdamUserDetails userDetails = caseData.getApplicantSolicitor1UserDetails();
        String submitterId = userDetails.getId();
        String organisationId = caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID();

        coreCaseUserService.assignCase(caseId, submitterId, organisationId, CaseRole.APPLICANTSOLICITORONE);
        coreCaseUserService.removeCreatorRoleCaseAssignment(caseId, submitterId, organisationId);

        CaseData updated = caseData.toBuilder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(userDetails.getEmail()).build())
            .build();

        getRespondentCaaAndAssignCase(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updated.toMap(objectMapper))
            .build();
    }

    private void getRespondentCaaAndAssignCase(CaseData caseData) {
        String organisationId;
        Optional<ProfessionalUsersEntityResponse> orgUsers;
        List<String> caaUserIds = new ArrayList<>();
        if (caseData.getRespondent1OrgRegistered() == YES
            && caseData.getRespondent1Represented() == YES) {
            organisationId = caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();

            //get users in this firm
            orgUsers = organisationService.findUsersInOrganisation(organisationId);


            ProfessionalUsersEntityResponse professionalUsersEntityResponse = orgUsers.orElse(null);
            if (professionalUsersEntityResponse != null) {
                for (ProfessionalUsersResponse user : professionalUsersEntityResponse.getUsers()) {
                    if (user.getRoles().contains(CASEWORKER_CAA_ROLE)) {
                        caaUserIds.add(user.getUserIdentifier());
                    }
                }
            }
            if (!caaUserIds.isEmpty()) {
                // assign case here
                assignCaseToRespondentCaa(caseData, organisationId, caaUserIds, CaseRole.RESPONDENTSOLICITORONE);
                System.out.println("case assigned to respondent");
            }
        }

        if (getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP) && caseData.getRespondent2OrgRegistered() == YES
            && caseData.getRespondent2Represented() == YES) {
            organisationId = caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID();

            orgUsers = organisationService.findUsersInOrganisation(organisationId);

            caaUserIds = new ArrayList<>();
            ProfessionalUsersEntityResponse professionalUsersEntityResponse = orgUsers.orElse(null);
            if (professionalUsersEntityResponse != null) {
                for (ProfessionalUsersResponse user : professionalUsersEntityResponse.getUsers()) {
                    if (user.getRoles().contains(CASEWORKER_CAA_ROLE)) {
                        caaUserIds.add(user.getUserIdentifier());
                    }
                }
            }
            if (!caaUserIds.isEmpty()) {
                // assign case here
                assignCaseToRespondentCaa(caseData, organisationId, caaUserIds, CaseRole.RESPONDENTSOLICITORTWO);
                System.out.println("case assigned to respondent");
            }
        }
    }

    private void assignCaseToRespondentCaa(CaseData caseData, String organisationId, List<String> caaUserIds, CaseRole caseRole) {
        for (String caaUserId : caaUserIds) {
            String caseId = caseData.getCcdCaseReference().toString();
            coreCaseUserService.assignCase(caseId, caaUserId, organisationId, caseRole);
        }
    }
}
