package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.PrepareEventTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.ShowWarningTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.SubmitChangesTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.ValidateExpertsTask;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.managecontactinformation.ValidateWitnessesTask;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.appendUserAndType;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapExpertsToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapWitnessesToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.prepareLRIndividuals;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.prepareOrgIndividuals;

@Service
@RequiredArgsConstructor
public class ManageContactInformationCallbackHandler extends CallbackHandler {

    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin", "caseworker-civil-staff");
    private static final List<CaseEvent> EVENTS = List.of(
        MANAGE_CONTACT_INFORMATION
    );

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final ValidateExpertsTask validateExpertsTask;
    private final ValidateWitnessesTask validateWitnessesTask;
    private final PrepareEventTask prepareEventTask;
    private final ShowWarningTask showWarningTask;
    private final SubmitChangesTask submitChangesTask;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prepareEvent)
            .put(callbackKey(MID, "show-party-field"), this::showPartyField)
            .put(callbackKey(MID, "show-warning"), this::showWarning)
            .put(callbackKey(MID, "validate-experts"), this::validateExperts)
            .put(callbackKey(MID, "validate-witnesses"), this::validateWitnesses)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitChanges)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateExperts(CallbackParams callbackParams) {
        return validateExpertsTask.validateExperts(callbackParams.getCaseData(), callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private CallbackResponse validateWitnesses(CallbackParams callbackParams) {
        return validateWitnessesTask.validateWitnesses(callbackParams.getCaseData(), callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private CallbackResponse prepareEvent(CallbackParams callbackParams) {
        return prepareEventTask.prepareEvent(callbackParams.getCaseData(), callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private List<Element<UpdatePartyDetailsForm>> prepareExperts(String partyId, CaseData caseData) {
        if (CLAIMANT_ONE_EXPERTS_ID.equals(partyId) && caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getExperts() != null) {
            return mapExpertsToUpdatePartyDetailsForm(caseData.getApplicant1DQ().getExperts());
        } else if (DEFENDANT_ONE_EXPERTS_ID.equals(partyId) && caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getExperts() != null) {
            return mapExpertsToUpdatePartyDetailsForm(caseData.getRespondent1DQ().getExperts());
        } else if (DEFENDANT_TWO_EXPERTS_ID.equals(partyId) && caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getExperts() != null) {
            return mapExpertsToUpdatePartyDetailsForm(caseData.getRespondent2DQ().getExperts());
        }
        return Collections.emptyList();
    }

    private List<Element<UpdatePartyDetailsForm>> prepareWitnesses(String partyId, CaseData caseData) {
        if (CLAIMANT_ONE_WITNESSES_ID.equals(partyId) && caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getWitnesses() != null) {
            return mapWitnessesToUpdatePartyDetailsForm(caseData.getApplicant1DQ().getWitnesses());
        } else if (DEFENDANT_ONE_WITNESSES_ID.equals(partyId) && caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getWitnesses() != null) {
            return mapWitnessesToUpdatePartyDetailsForm(caseData.getRespondent1DQ().getWitnesses());
        } else if (DEFENDANT_TWO_WITNESSES_ID.equals(partyId) && caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getWitnesses() != null) {
            return mapWitnessesToUpdatePartyDetailsForm(caseData.getRespondent2DQ().getWitnesses());
        }
        return Collections.emptyList();
    }

    private CallbackResponse showWarning(CallbackParams callbackParams) {
        return showWarningTask.showWarning(callbackParams.getCaseData(), callbackParams.getRequest().getCaseDetailsBefore());
    }

    private CallbackResponse showPartyField(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosen().getValue().getCode();
        String partyChosenType = null;

        if (isParty(partyChosen) || isLitigationFriend(partyChosen)) {
            // Party fields are empty in this mid event, this is a workaround
            CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());
            String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
            boolean isAdmin = isAdmin(authToken);
            partyChosenType = appendUserAndType(partyChosen, oldCaseData, isAdmin);
        }

        UpdateDetailsForm.UpdateDetailsFormBuilder formBuilder = caseData.getUpdateDetailsForm().toBuilder()
            .partyChosenId(partyChosen)
            .partyChosenType(partyChosenType)
            .updateExpertsDetailsForm(prepareExperts(partyChosen, caseData))
            .updateWitnessesDetailsForm(prepareWitnesses(partyChosen, caseData))
            .updateLRIndividualsForm(prepareLRIndividuals(partyChosen, caseData))
            .updateOrgIndividualsForm(prepareOrgIndividuals(partyChosen, caseData))
            .build().toBuilder();

        builder.updateDetailsForm(formBuilder.build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private Boolean isParty(String partyChosen) {
        return CLAIMANT_ONE_ID.equals(partyChosen)
            || CLAIMANT_TWO_ID.equals(partyChosen)
            || DEFENDANT_ONE_ID.equals(partyChosen)
            || DEFENDANT_TWO_ID.equals(partyChosen);
    }

    private Boolean isLitigationFriend(String partyChosen) {
        return CLAIMANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen)
            || CLAIMANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen)
            || DEFENDANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen)
            || DEFENDANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen);
    }

    private CallbackResponse submitChanges(CallbackParams callbackParams) {
        return submitChangesTask.submitChanges(callbackParams.getCaseData(),
                                               callbackParams.getRequest().getCaseDetailsBefore(),
                                               callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Contact information changed")
            .confirmationBody("### What happens next\nAny changes made to contact details have been updated.")
            .build();
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(ADMIN_ROLES::contains);
    }

}
