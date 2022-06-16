package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.AutomaticallyAssignCaseToCaaConfiguration;
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

@Slf4j
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
    private final AutomaticallyAssignCaseToCaaConfiguration automaticallyAssignCaseToCaaConfiguration;

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

        if (automaticallyAssignCaseToCaaConfiguration.isAssignCaseToCaa()) {
            getRespondentCaaAndAssignCase(caseData);
            log.info("Automatically assigned case to respondent caa");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updated.toMap(objectMapper))
            .build();
    }

    private void getRespondentCaaAndAssignCase(CaseData caseData) {
        if (caseData.getRespondent1OrgRegistered() == YES
            && caseData.getRespondent1Represented() == YES) {
            assignCaseToRespondentCaa(caseData,
                                      caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID(),
                                      CaseRole.RESPONDENTSOLICITORONE);
        }

        if (getMultiPartyScenario(caseData).equals(ONE_V_TWO_TWO_LEGAL_REP)
            && caseData.getRespondent2OrgRegistered() == YES
            && caseData.getRespondent2Represented() == YES) {
            assignCaseToRespondentCaa(caseData,
                                      caseData.getRespondent2OrganisationPolicy().getOrganisation().getOrganisationID(),
                                      CaseRole.RESPONDENTSOLICITORTWO);
        }
    }

    private void assignCaseToRespondentCaa(CaseData caseData, String organisationId, CaseRole caseRole) {
        List<String> caaUserIds;
        caaUserIds = new ArrayList<>();
        String caseId = caseData.getCcdCaseReference().toString();

        Optional<ProfessionalUsersEntityResponse> orgUsers =
            organisationService.findUsersInOrganisation(organisationId);

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = orgUsers.orElse(null);

        if (professionalUsersEntityResponse != null) {
            for (ProfessionalUsersResponse user : professionalUsersEntityResponse.getUsers()) {
                log.info("about to get roles for user {}", user.getEmail());
                boolean nullUserRoles = user.getRoles() == null;
                boolean emptyUserRoles = user.getRoles().isEmpty();
                log.info("null user roles? {}, empty user roles {}", nullUserRoles, emptyUserRoles);
                if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                    log.info("user ID {} roles {}", user.getEmail(), user.getRoles().toString());
                    if (user.getRoles().contains(CASEWORKER_CAA_ROLE)) {
                        caaUserIds.add(user.getUserIdentifier());
                        log.info("adding caa user with ID {}, username {}", user.getUserIdentifier(), user.getEmail());
                    }
                }
            }
        } else {
            log.info("prof users entity response is null");
        }
        if (!caaUserIds.isEmpty()) {
            for (String caaUserId : caaUserIds) {
                coreCaseUserService.assignCase(caseId, caaUserId, organisationId, caseRole);
            }
        } else {
            log.info("No caa users found for org ID {} for case {}", organisationId, caseId);
        }
    }
}
