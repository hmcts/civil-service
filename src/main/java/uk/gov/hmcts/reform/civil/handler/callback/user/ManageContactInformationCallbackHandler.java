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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ContactDetailsUpdatedEvent;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CaseNameUtils;
import uk.gov.hmcts.reform.civil.utils.PartyDetailsChangedUtil;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CONTACT_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseName;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.CLAIMANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_ONE_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_EXPERTS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_LITIGATION_FRIEND_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_ORG_INDIVIDUALS_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.DEFENDANT_TWO_WITNESSES_ID;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.appendUserAndType;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapExpertsToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapFormDataToIndividualsData;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapWitnessesToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.prepareLRIndividuals;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.prepareOrgIndividuals;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.updatePartyDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.updatePartyDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populatePartyIndividuals;
import static uk.gov.hmcts.reform.civil.utils.PersistDataUtils.persistFlagsForLitigationFriendParties;
import static uk.gov.hmcts.reform.civil.utils.PersistDataUtils.persistFlagsForParties;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class ManageContactInformationCallbackHandler extends CallbackHandler {

    private static final String INVALID_CASE_STATE_ERROR = "You will be able to run the manage contact information " +
        "event once the claimant has responded.";
    private static final String CHECK_LITIGATION_FRIEND_ERROR_TITLE = "Check the litigation friend's details";
    private static final String CHECK_LITIGATION_FRIEND_ERROR = "After making these changes, please ensure that the "
        + "litigation friend's contact information is also up to date.";
    private static final String CHECK_LITIGATION_FRIEND_WARNING = "There is another litigation friend on this case. "
        + "If the parties are using the same litigation friend you must update the other litigation friend's details too.";
    private static final String CREATE_ORDER_ERROR_EXPERTS = "Adding a new expert is not permitted in this screen. Please delete any new experts.";
    private static final String CREATE_ORDER_ERROR_WITNESSES = "Adding a new witness is not permitted in this screen. Please delete any new witnesses.";
    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin", "caseworker-civil-staff");
    private static final List<CaseEvent> EVENTS = List.of(
        MANAGE_CONTACT_INFORMATION
    );

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final PostcodeValidator postcodeValidator;
    private final PartyDetailsChangedUtil partyDetailsChangedUtil;

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
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        // Legal Reps should not be able to add or delete experts and witnesses.
        // Have to add "CRU" for LRs for updateExpertsDetailsForm or else we see a No Field Found error upon submission.
        if (!isAdmin(authToken)) {
            List<UpdatePartyDetailsForm> expertsWithoutPartyId = unwrapElements(caseData.getUpdateDetailsForm().getUpdateExpertsDetailsForm())
                .stream()
                .filter(e -> e.getPartyId() == null)
                .toList();

            if (!expertsWithoutPartyId.isEmpty()) {
                errors.add(CREATE_ORDER_ERROR_EXPERTS);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse validateWitnesses(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        // Legal Reps should not be able to add or delete experts and witnesses.
        // Have to add "CRU" for LRs for UpdateWitnessesDetailsForm or else we see a No Field Found error upon submission.
        if (!isAdmin(authToken)) {
            List<UpdatePartyDetailsForm> witnessesWithoutPartyId = unwrapElements(caseData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm())
                .stream()
                .filter(e -> e.getPartyId() == null)
                .toList();

            if (!witnessesWithoutPartyId.isEmpty()) {
                errors.add(CREATE_ORDER_ERROR_WITNESSES);
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse prepareEvent(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();

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
            callbackParams.getCaseData().getCcdCaseReference().toString(),
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
                case ONE_V_TWO_ONE_LEGAL_REP -> {
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
                case ONE_V_ONE, ONE_V_TWO_ONE_LEGAL_REP, ONE_V_TWO_TWO_LEGAL_REP ->
                    addApplicant1Options(dynamicListOptions, caseData, false);
                case TWO_V_ONE -> addApplicantOptions2v1(dynamicListOptions, caseData, false);
                default -> throw new CallbackException(invalidParticipants);
            }
        } else if (isRespondentSolicitorOne(roles)) {
            switch (multiPartyScenario) {
                case ONE_V_ONE, ONE_V_TWO_TWO_LEGAL_REP, TWO_V_ONE ->
                    addDefendant1Options(dynamicListOptions, caseData, false);
                case ONE_V_TWO_ONE_LEGAL_REP ->
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

    private String getPostCode(String partyChosen, CaseData caseData) {
        switch (partyChosen) {
            case CLAIMANT_ONE_ID: {
                return getPartyPostCode(caseData.getApplicant1());
            }
            case CLAIMANT_TWO_ID: {
                return getPartyPostCode(caseData.getApplicant2());
            }
            case DEFENDANT_ONE_ID: {
                return getPartyPostCode(caseData.getRespondent1());
            }
            case DEFENDANT_TWO_ID: {
                return getPartyPostCode(caseData.getRespondent2());
            }
            case CLAIMANT_ONE_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getApplicant1LitigationFriend());
            }
            case CLAIMANT_TWO_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getApplicant2LitigationFriend());
            }
            case DEFENDANT_ONE_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getRespondent1LitigationFriend());
            }
            case DEFENDANT_TWO_LITIGATION_FRIEND_ID: {
                return getPartyPostCode(caseData.getRespondent2LitigationFriend());
            }
            default: {
                return null;
            }
        }
    }

    private String getPartyPostCode(Party party) {
        return party.getPrimaryAddress().getPostCode();
    }

    private String getPartyPostCode(LitigationFriend party) {
        return party.getPrimaryAddress().getPostCode();
    }

    private CallbackResponse showWarning(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosen().getValue().getCode();
        ArrayList<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());

        // oldCaseData needed because Litigation friend gets nullified in mid event
        if (partyHasLitigationFriend(partyChosen, oldCaseData)) {
            warnings.add(CHECK_LITIGATION_FRIEND_ERROR_TITLE);
            warnings.add(CHECK_LITIGATION_FRIEND_ERROR);
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            errors = postcodeValidator.validate(getPostCode(partyChosen, caseData));
        }

        if (showLitigationFriendUpdateWarning(partyChosen, oldCaseData)) {
            warnings.add(CHECK_LITIGATION_FRIEND_WARNING);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .warnings(warnings)
            .errors(errors)
            .build();
    }

    private Boolean partyHasLitigationFriend(String partyChosen, CaseData caseData) {
        return hasLitigationFriend(CLAIMANT_ONE_ID, partyChosen, caseData.getApplicant1LitigationFriendRequired())
            || hasLitigationFriend(CLAIMANT_TWO_ID, partyChosen, caseData.getApplicant2LitigationFriendRequired())
            || hasLitigationFriend(DEFENDANT_ONE_ID, partyChosen, caseData.getRespondent1LitigationFriend())
            || hasLitigationFriend(DEFENDANT_TWO_ID, partyChosen, caseData.getRespondent2LitigationFriend());
    }

    private Boolean hasLitigationFriend(String id, String partyChosen, YesOrNo litigationFriend) {
        return id.equals(partyChosen) && YES.equals(litigationFriend);
    }

    private Boolean hasLitigationFriend(String id, String partyChosen, LitigationFriend litigationFriend) {
        return id.equals(partyChosen) && litigationFriend != null;
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
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder builder = caseData.toBuilder();
        CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());

        // persist party flags (ccd issue)
        persistFlagsForParties(oldCaseData, caseData, builder);
        persistFlagsForLitigationFriendParties(oldCaseData, caseData, builder);

        String partyChosenId = caseData.getUpdateDetailsForm().getPartyChosenId();

        updateExperts(partyChosenId, caseData, builder);
        updateWitnesses(partyChosenId, caseData, builder);
        updateLRIndividuals(partyChosenId, caseData, builder);
        updateOrgIndividuals(partyChosenId, caseData, builder);

        if (isParty(partyChosenId) || isLitigationFriend(partyChosenId)) {
            // update case name for hmc if applicant/respondent/litigation friend was updated
            builder.caseNameHmctsInternal(CaseNameUtils.buildCaseName(caseData));
            builder.caseNamePublic(buildCaseName(caseData));
        }

        // last step before clearing update details form
        caseFlagsInitialiser.initialiseCaseFlags(MANAGE_CONTACT_INFORMATION, builder);

        // clear updateDetailsForm
        builder.updateDetailsForm(UpdateDetailsForm.builder().manageContactDetailsEventUsed(YES).build());

        // update claim details tab
        updateClaimDetailsTab(caseData, builder);

        CaseData current = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());
        ContactDetailsUpdatedEvent changesEvent = partyDetailsChangedUtil.buildChangesEvent(current, builder.build());
        //Populate individuals with partyID if they do not exist
        populatePartyIndividuals(builder);

        if (changesEvent == null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                    .data(builder.build().toMap(objectMapper))
                    .build();
        }

        YesOrNo submittedByCaseworker = isAdmin(callbackParams.getParams().get(BEARER_TOKEN).toString()) ? YES : NO;

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(builder
                        .businessProcess(BusinessProcess.ready(MANAGE_CONTACT_INFORMATION))
                        .contactDetailsUpdatedEvent(
                                changesEvent.toBuilder()
                                        .submittedByCaseworker(submittedByCaseworker)
                                        .build())
                        .build().toMap(objectMapper))
                .build();
    }

    private void updateClaimDetailsTab(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        builder.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1().toBuilder().flags(null).build());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            builder.respondent2DetailsForClaimDetailsTab(caseData.getRespondent2().toBuilder().flags(null).build());
        }
    }

    // wip can't be tested yet because need to get ids from new ticket: CIV-10382
    // have to delete experts (yes/no etc) if the experts are removed, same as witnesses

    private void updateExperts(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        List<Element<UpdatePartyDetailsForm>> formData = caseData.getUpdateDetailsForm().getUpdateExpertsDetailsForm();
        List<Element<Expert>> mappedExperts;

        if (partyId.equals(CLAIMANT_ONE_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getApplicant1DQ().getApplicant1DQExperts(), formData);
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                     .applicant1DQExperts(
                                         buildExperts(caseData.getApplicant1DQ().getApplicant1DQExperts(), mappedExperts))
                                     .build());
            List<Element<PartyFlagStructure>> updatedApplicantExperts = updatePartyDQExperts(
                unwrapElements(caseData.getApplicantExperts()),
                unwrapElements(mappedExperts)
            );
            builder.applicantExperts(updatedApplicantExperts);

            // copy in applicant 2 for single response
            if (shouldCopyToApplicant2(caseData)) {
                builder.applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                         .applicant2DQExperts(
                                             buildExperts(caseData.getApplicant1DQ().getApplicant1DQExperts(), mappedExperts))
                                         .build());
            }

        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent1DQ().getRespondent1DQExperts(), formData);
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                     .respondent1DQExperts(
                                         buildExperts(caseData.getRespondent1DQ().getRespondent1DQExperts(), mappedExperts))
                                     .build());
            List<Element<PartyFlagStructure>> updatedRespondent1Experts = updatePartyDQExperts(
                unwrapElements(caseData.getRespondent1Experts()),
                unwrapElements(mappedExperts)
            );
            builder.respondent1Experts(updatedRespondent1Experts);

            // copy in respondent2 for 1v2SS single response
            if (shouldCopyToRespondent2(caseData)) {
                builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                          .respondent2DQExperts(
                                              buildExperts(caseData.getRespondent1DQ().getRespondent1DQExperts(), mappedExperts))
                                          .build());
                builder.respondent2Experts(updatedRespondent1Experts);
            }
        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent2DQ().getRespondent2DQExperts(), formData);
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                     .respondent2DQExperts(
                                         buildExperts(caseData.getRespondent2DQ().getRespondent2DQExperts(), mappedExperts))
                                     .build());
            List<Element<PartyFlagStructure>> updatedRespondent2Experts = updatePartyDQExperts(
                unwrapElements(caseData.getRespondent2Experts()),
                unwrapElements(mappedExperts)
            );
            builder.respondent2Experts(updatedRespondent2Experts);
        }
    }

    private Experts buildExperts(Experts experts, List<Element<Expert>> mappedExperts) {
        return ofNullable(experts)
            .orElse(Experts.builder().build())
            .toBuilder()
            .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
            .details(mappedExperts).build();
    }

    private Witnesses buildWitnesses(Witnesses witnesses, List<Element<Witness>> mappedWitnesses) {
        return ofNullable(witnesses)
            .orElse(Witnesses.builder().build())
            .toBuilder()
            .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
            .details(mappedWitnesses).build();
    }

    private void updateWitnesses(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        List<Element<UpdatePartyDetailsForm>> formData = caseData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm();
        List<Element<Witness>> mappedWitnesses;

        if (partyId.equals(CLAIMANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getApplicant1DQ().getApplicant1DQWitnesses(), formData);
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                     .applicant1DQWitnesses(
                                         buildWitnesses(caseData.getApplicant1DQ().getApplicant1DQWitnesses(), mappedWitnesses))
                                     .build());
            List<Element<PartyFlagStructure>> updatedApplicantWitnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getApplicantWitnesses()),
                unwrapElements(mappedWitnesses)
            );
            builder.applicantWitnesses(updatedApplicantWitnesses);

            // copy in applicant 2 for single response
            if (shouldCopyToApplicant2(caseData)) {
                builder.applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                         .applicant2DQWitnesses(
                                             buildWitnesses(caseData.getApplicant1DQ().getApplicant1DQWitnesses(), mappedWitnesses))
                                         .build());
            }
        } else if (partyId.equals(DEFENDANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent1DQ().getRespondent1DQWitnesses(), formData);
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                 .respondent1DQWitnesses(
                                     buildWitnesses(caseData.getRespondent1DQ().getRespondent1DQWitnesses(), mappedWitnesses))
                                 .build());
            List<Element<PartyFlagStructure>> updatedRespondent1Witnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getRespondent1Witnesses()),
                unwrapElements(mappedWitnesses)
            );
            builder.respondent1Witnesses(updatedRespondent1Witnesses);

            // copy in respondent2 for 1v2SS single response
            if (shouldCopyToRespondent2(caseData)) {
                builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                          .respondent2DQWitnesses(
                                              buildWitnesses(caseData.getRespondent1DQ().getRespondent1DQWitnesses(), mappedWitnesses))
                                          .build());
                builder.respondent2Witnesses(updatedRespondent1Witnesses);
            }
        } else if (partyId.equals(DEFENDANT_TWO_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent2DQ().getRespondent2DQWitnesses(), formData);
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                 .respondent2DQWitnesses(
                                     buildWitnesses(caseData.getRespondent2DQ().getRespondent2DQWitnesses(), mappedWitnesses))
                                 .build());
            List<Element<PartyFlagStructure>> updatedRespondent2Witnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getRespondent2Witnesses()),
                unwrapElements(mappedWitnesses)
            );
            builder.respondent2Witnesses(updatedRespondent2Witnesses);
        }
    }

    private void updateLRIndividuals(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        switch (partyId) {
            case CLAIMANT_ONE_LEGAL_REP_INDIVIDUALS_ID: {
                builder.applicant1LRIndividuals(mapFormDataToIndividualsData(caseData.getApplicant1LRIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateLRIndividualsForm()));
                return;
            }
            case DEFENDANT_ONE_LEGAL_REP_INDIVIDUALS_ID: {
                builder.respondent1LRIndividuals(mapFormDataToIndividualsData(caseData.getRespondent1LRIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateLRIndividualsForm()));
                return;
            }
            case DEFENDANT_TWO_LEGAL_REP_INDIVIDUALS_ID: {
                builder.respondent2LRIndividuals(mapFormDataToIndividualsData(caseData.getRespondent2LRIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateLRIndividualsForm()));
                return;
            }
            default:
        }
    }

    private void updateOrgIndividuals(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        switch (partyId) {
            case CLAIMANT_ONE_ORG_INDIVIDUALS_ID: {
                builder.applicant1OrgIndividuals(mapFormDataToIndividualsData(caseData.getApplicant1OrgIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            case CLAIMANT_TWO_ORG_INDIVIDUALS_ID: {
                builder.applicant2OrgIndividuals(mapFormDataToIndividualsData(caseData.getApplicant2OrgIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            case DEFENDANT_ONE_ORG_INDIVIDUALS_ID: {
                builder.respondent1OrgIndividuals(mapFormDataToIndividualsData(caseData.getRespondent1OrgIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            case DEFENDANT_TWO_ORG_INDIVIDUALS_ID: {
                builder.respondent2OrgIndividuals(mapFormDataToIndividualsData(caseData.getRespondent2OrgIndividuals(),
                        caseData.getUpdateDetailsForm().getUpdateOrgIndividualsForm()));
                return;
            }
            default:
        }
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Contact information changed")
            .confirmationBody("### What happens next\nAny changes made to contact details have been updated in the Claim Details tab.")
            .build();
    }

    private boolean shouldCopyToRespondent2(CaseData caseData) {
        return caseData.getRespondent2() != null
            && YES.equals(caseData.getRespondent2SameLegalRepresentative())
            && YES.equals(caseData.getRespondentResponseIsSame());
    }

    private boolean shouldCopyToApplicant2(CaseData caseData) {
        return caseData.getApplicant2() != null
            && ((YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
            && YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1()))
            || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1()));
    }

    private boolean isBeforeAwaitingApplicantIntention(CaseData caseData) {
        return caseData.getCcdState().equals(AWAITING_APPLICANT_INTENTION) || caseData.getCcdState().equals(AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(ADMIN_ROLES::contains);
    }

    private boolean showLitigationFriendUpdateWarning(String partyChosen, CaseData caseData) {
        return ((CLAIMANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen) || CLAIMANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen))
            && bothClaimantsHaveLitigationFriends(caseData))
            || ((DEFENDANT_ONE_LITIGATION_FRIEND_ID.equals(partyChosen) || DEFENDANT_TWO_LITIGATION_FRIEND_ID.equals(partyChosen))
            && ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && bothDefendantsHaveLitigationFriends(caseData));
    }

    private boolean bothClaimantsHaveLitigationFriends(CaseData caseData) {
        return nonNull(caseData.getApplicant1LitigationFriend()) && nonNull(caseData.getApplicant2LitigationFriend());
    }

    private boolean bothDefendantsHaveLitigationFriends(CaseData caseData) {
        return nonNull(caseData.getRespondent1LitigationFriend()) && nonNull(caseData.getRespondent2LitigationFriend());
    }

}
