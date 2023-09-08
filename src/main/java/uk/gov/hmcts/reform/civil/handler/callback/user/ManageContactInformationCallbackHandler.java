package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.appendUserAndType;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapDQExpertsToPartyExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapExpertsToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQExperts;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class ManageContactInformationCallbackHandler extends CallbackHandler {

    private static final String INVALID_CASE_STATE_ERROR = "You will be able run the manage contact information " +
        "event once the claimant has responded.";
    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin");
    private static final List<CaseEvent> EVENTS = List.of(
        MANAGE_CONTACT_INFORMATION
    );

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prepareEvent)
            .put(callbackKey(MID, "show-party-field"), this::showPartyField)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitChanges)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse showPartyField(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        boolean isAdmin = isAdmin(authToken);
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosen().getValue().getCode();
        String partyChosenType = null;

        if (CLAIMANT_ONE_ID.equals(partyChosen)
            || CLAIMANT_TWO_ID.equals(partyChosen)
            || DEFENDANT_ONE_ID.equals(partyChosen)
            || DEFENDANT_TWO_ID.equals(partyChosen)) {
            // Party fields are empty in this mid event, this is a workaround
            CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());
            partyChosenType = appendUserAndType(partyChosen, oldCaseData, isAdmin);
        }

        UpdateDetailsForm.UpdateDetailsFormBuilder formBuilder = caseData.getUpdateDetailsForm().toBuilder()
            .partyChosenId(partyChosen)
            .partyChosenType(partyChosenType)
            .updateExpertsDetailsForm(prepareExperts(partyChosen, caseData))
            .updateWitnessesDetailsForm(prepareWitnesses(partyChosen, caseData))
            .build().toBuilder();

        builder.updateDetailsForm(formBuilder.build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse prepareEvent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

        UserInfo userInfo = userService.getUserInfo(authToken);
        boolean isAdmin = isAdmin(authToken);

        List<String> errors = isAwaitingClaimantIntention(caseData)
            && !isAdmin ? List.of(INVALID_CASE_STATE_ERROR) : null;

        if (errors != null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        List<String> roles = coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );

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
                case ONE_V_TWO_ONE_LEGAL_REP -> {
                    addApplicant1Options(dynamicListOptions, caseData, true);
                    addDefendantOptions1v2SameSolicitor(dynamicListOptions, caseData, true);
                }
                case ONE_V_TWO_TWO_LEGAL_REP -> {
                    addApplicant1Options(dynamicListOptions, caseData, true);
                    addDefendant1Options(dynamicListOptions, caseData, true);
                    addDefendant2Options(dynamicListOptions, caseData, true);
                }
                default -> throw new CallbackException("Invalid participants");
            }
        } else if (isApplicantSolicitor(roles)) {
            switch (multiPartyScenario) {
                case ONE_V_ONE, ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    addApplicant1Options(dynamicListOptions, caseData, false);
                case TWO_V_ONE -> addApplicantOptions2v1(dynamicListOptions, caseData, false);
                default -> throw new CallbackException("Invalid participants");
            }
        } else if (isRespondentSolicitorOne(roles)) {
            switch (multiPartyScenario) {
                case ONE_V_ONE, ONE_V_TWO_TWO_LEGAL_REP, TWO_V_ONE ->
                    addDefendant1Options(dynamicListOptions, caseData, false);
                case ONE_V_TWO_ONE_LEGAL_REP ->
                    addDefendantOptions1v2SameSolicitor(dynamicListOptions, caseData, false);
                default -> throw new CallbackException("Invalid participants");
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

    private List<Element<UpdatePartyDetailsForm>> prepareExperts(String partyId, CaseData caseData) {
        if (partyId.equals(CLAIMANT_ONE_EXPERTS_ID)) {
            return mapExpertsToUpdatePartyDetailsForm(caseData.getApplicant1DQ().getExperts().getDetails());
        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
            return mapExpertsToUpdatePartyDetailsForm(caseData.getRespondent1DQ().getExperts().getDetails());
        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
            return mapExpertsToUpdatePartyDetailsForm(caseData.getRespondent2DQ().getExperts().getDetails());
        }
        return Collections.emptyList();
    }

    private List<Element<UpdatePartyDetailsForm>> prepareWitnesses(String partyId, CaseData caseData) {
//        if (partyId.equals(CLAIMANT_ONE_EXPERTS_ID)) {
//        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
//        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
//        }
        return Collections.emptyList();
    }

    private CallbackResponse submitChanges(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();

//        updateExperts(caseData.getUpdateDetailsForm().getPartyChosenId(), caseData, builder);

        // clear updateDetailsForm
        builder.updateDetailsForm(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .build();
    }

    // wip can't be tested yet because need to get ids from new ticket: CIV-10382

//    private void updateExperts(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
//        if (partyId.equals(CLAIMANT_ONE_EXPERTS_ID)) {
//            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
//                                     .applicant1DQExperts(
//                                         caseData.getApplicant1DQ().getApplicant1DQExperts().toBuilder()
//                                             .details(getDQExperts(caseData))
//                                             .build())
//                                     .build())
//                .applicantExperts(getPartyExperts(caseData));
//        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
//            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
//                                     .respondent1DQExperts(
//                                         caseData.getRespondent1DQ().getRespondent1DQExperts().toBuilder()
//                                             .details(getDQExperts(caseData))
//                                             .build())
//                                     .build())
//                .respondent1Experts(getPartyExperts(caseData));
//        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
//            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
//                                     .respondent2DQExperts(
//                                         caseData.getRespondent2DQ().getRespondent2DQExperts().toBuilder()
//                                             .details(getDQExperts(caseData))
//                                             .build())
//                                     .build())
//                .respondent2Experts(getPartyExperts(caseData));
//        }
//    }
//
//    private List<Element<Expert>> getDQExperts(CaseData caseData){
//        return mapUpdatePartyDetailsFormToDQExperts(
//            caseData.getApplicant1DQ().getExperts().getDetails(),
//            caseData.getUpdateDetailsForm().getUpdateExpertsDetailsForm());
//    }
//
//    private List<Element<PartyFlagStructure>> getPartyExperts(CaseData caseData){
//        return mapDQExpertsToPartyExperts(
//            caseData.getApplicant1DQ().getExperts().getDetails(),
//            caseData.getApplicantExperts());
//    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Contact information changed"))
            .confirmationBody(format("### What happens next\nAny changes made to contact details have been updated in the Claim Details tab."))
            .build();
    }

    private boolean isAwaitingClaimantIntention(CaseData caseData) {
        return caseData.getCcdState().equals(AWAITING_APPLICANT_INTENTION);
    }

    private boolean isAdmin(String userAuthToken) {
    return userService.getUserInfo(userAuthToken).getRoles()
        .stream().anyMatch(ADMIN_ROLES::contains);
    }
}
