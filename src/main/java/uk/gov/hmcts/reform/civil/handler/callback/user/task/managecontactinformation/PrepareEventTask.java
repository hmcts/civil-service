package uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Slf4j
@Component
public class PrepareEventTask {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final CoreCaseUserService coreCaseUserService;

    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin", "caseworker-civil-staff");
    private static final String INVALID_CASE_STATE_ERROR = "You will be able to run the manage contact information " +
        "event once the claimant has responded.";

    public PrepareEventTask(UserService userService, ObjectMapper objectMapper, CoreCaseUserService coreCaseUserService) {
        this.userService = userService;
        this.objectMapper = objectMapper;
        this.coreCaseUserService = coreCaseUserService;
    }

    public CallbackResponse prepareEvent(CaseData caseData, String authToken) {

        log.info("Prepare event for case ID {}", caseData.getCcdCaseReference());
        UserInfo userInfo = userService.getUserInfo(authToken);
        boolean isAdmin = isAdmin(authToken);

        List<String> errors = isBeforeAwaitingApplicantIntention(caseData)
            && !isAdmin ? List.of(INVALID_CASE_STATE_ERROR) : null;

        if (errors != null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        List<String> roles = coreCaseUserService.getUserCaseRoles(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid()
        );

        final String invalidParticipants = "Invalid participants";

        if (isAdmin) {
            switch (multiPartyScenario) {
                case ONE_V_ONE -> {
                    addApplicant1Options(dynamicListOptions, caseData, true);
                    addDefendant1Options(dynamicListOptions, caseData, true);
                }
                case TWO_V_ONE -> {
                    addApplicantOptions2v1(dynamicListOptions, caseData, true);
                    addDefendant1Options(dynamicListOptions, caseData, true);
                }
                case ONE_V_TWO_SAME_LEGAL_REP -> {
                    addApplicant1Options(dynamicListOptions, caseData, true);
                    addDefendantOptions1v2SameSolicitor(dynamicListOptions, caseData, true);
                }
                case ONE_V_TWO_TWO_LEGAL_REP -> {
                    addApplicant1Options(dynamicListOptions, caseData, true);
                    addDefendant1Options(dynamicListOptions, caseData, true);
                    addDefendant2Options(dynamicListOptions, caseData, true);
                }
                default -> throw new CallbackException(invalidParticipants);
            }
        } else if (isApplicantSolicitor(roles)) {
            switch (multiPartyScenario) {
                case ONE_V_ONE, ONE_V_TWO_SAME_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    addApplicant1Options(dynamicListOptions, caseData, false);
                case TWO_V_ONE -> addApplicantOptions2v1(dynamicListOptions, caseData, false);
                default -> throw new CallbackException(invalidParticipants);
            }
        } else if (isRespondentSolicitorOne(roles)) {
            switch (multiPartyScenario) {
                case ONE_V_ONE, ONE_V_TWO_TWO_LEGAL_REP, TWO_V_ONE ->
                    addDefendant1Options(dynamicListOptions, caseData, false);
                case ONE_V_TWO_SAME_LEGAL_REP ->
                    addDefendantOptions1v2SameSolicitor(dynamicListOptions, caseData, false);
                default -> throw new CallbackException(invalidParticipants);
            }
        } else if (isRespondentSolicitorTwo(roles)
            && ONE_V_TWO_TWO_LEGAL_REP.equals(multiPartyScenario)) {
            addDefendant2Options(dynamicListOptions, caseData, false);
        }

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder().updateDetailsForm(
            UpdateDetailsForm.builder()
                .partyChosen(DynamicList.fromDynamicListElementList(dynamicListOptions))
                .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(ADMIN_ROLES::contains);
    }

    private boolean isBeforeAwaitingApplicantIntention(CaseData caseData) {
        return caseData.getCcdState().equals(AWAITING_APPLICANT_INTENTION) || caseData.getCcdState().equals(AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
    }

}
