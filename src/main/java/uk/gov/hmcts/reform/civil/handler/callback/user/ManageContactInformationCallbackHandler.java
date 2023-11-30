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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpdatePartyDetailsForm;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.lang.String.format;
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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseNameInternal;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseNamePublic;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
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
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addApplicantOptions2v1;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant1Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendant2Options;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.addDefendantOptions1v2SameSolicitor;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.appendUserAndType;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapExpertsToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapUpdatePartyDetailsFormToDQWitnesses;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.mapWitnessesToUpdatePartyDetailsForm;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.updatePartyDQExperts;
import static uk.gov.hmcts.reform.civil.utils.ManageContactInformationUtils.updatePartyDQWitnesses;
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
    private static final String CREATE_ORDER_ERROR_EXPERTS = "Please create an order to add more experts.";
    private static final String CREATE_ORDER_ERROR_WITNESSES = "Please create an order to add more witnesses.";
    private static final List<String> ADMIN_ROLES = List.of(
        "caseworker-civil-admin");
    private static final List<CaseEvent> EVENTS = List.of(
        MANAGE_CONTACT_INFORMATION
    );

    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final PostcodeValidator postcodeValidator;

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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosen().getValue().getCode();
        ArrayList<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (partyHasLitigationFriend(partyChosen, caseData)) {
            warnings.add(CHECK_LITIGATION_FRIEND_ERROR_TITLE);
            warnings.add(CHECK_LITIGATION_FRIEND_ERROR);
        }

        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            errors = postcodeValidator.validate(getPostCode(partyChosen, caseData));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .warnings(warnings)
            .errors(errors)
            .build();
    }

    private Boolean partyHasLitigationFriend(String partyChosen, CaseData caseData) {
        if (hasLitigationFriend(CLAIMANT_ONE_ID, partyChosen, caseData.getApplicant1LitigationFriendRequired())
            || hasLitigationFriend(CLAIMANT_TWO_ID, partyChosen, caseData.getApplicant2LitigationFriendRequired())
            || hasLitigationFriend(DEFENDANT_ONE_ID, partyChosen, caseData.getRespondent1LitigationFriend())
            || hasLitigationFriend(DEFENDANT_TWO_ID, partyChosen, caseData.getRespondent2LitigationFriend())
        ) {
            return true;
        }
        return false;
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
        String partyChosenId = caseData.getUpdateDetailsForm().getPartyChosenId();

        updateExperts(partyChosenId, caseData, builder);
        updateWitnesses(partyChosenId, caseData, builder);

        // persist party flags (ccd issue)
        if (isParty(partyChosenId)) {
            getFlagsForParty(callbackParams, caseData, builder);
        }

        if (isParty(partyChosenId) || isLitigationFriend(partyChosenId)) {
            // update case name for hmc if applicant/respondent/litigation friend was updated
            builder.caseNameHmctsInternal(buildCaseNameInternal(caseData));
            builder.caseNamePublic(buildCaseNamePublic(caseData));
        }

        // last step before clearing update details form
        caseFlagsInitialiser.initialiseCaseFlags(MANAGE_CONTACT_INFORMATION, builder);

        // clear updateDetailsForm
        builder.updateDetailsForm(UpdateDetailsForm.builder().manageContactDetailsEventUsed(YES).build());

        // update claim details tab
        updateClaimDetailsTab(caseData, builder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(builder.build().toMap(objectMapper))
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
                                         caseData.getApplicant1DQ().getApplicant1DQExperts().toBuilder()
                                             .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
                                             .details(mappedExperts)
                                             .build())
                                     .build());
            List<Element<PartyFlagStructure>> updatedApplicantExperts = updatePartyDQExperts(
                unwrapElements(caseData.getApplicantExperts()),
                unwrapElements(mappedExperts)
            );
            builder.applicantExperts(updatedApplicantExperts);
        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent1DQ().getRespondent1DQExperts(), formData);
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                     .respondent1DQExperts(
                                         caseData.getRespondent1DQ().getRespondent1DQExperts().toBuilder()
                                             .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
                                             .details(mappedExperts)
                                             .build())
                                     .build());
            List<Element<PartyFlagStructure>> updatedRespondent1Experts = updatePartyDQExperts(
                unwrapElements(caseData.getRespondent1Experts()),
                unwrapElements(mappedExperts)
            );
            builder.respondent1Experts(updatedRespondent1Experts);
        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent2DQ().getRespondent2DQExperts(), formData);
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                     .respondent2DQExperts(
                                         caseData.getRespondent2DQ().getRespondent2DQExperts().toBuilder()
                                             .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
                                             .details(mappedExperts)
                                             .build())
                                     .build());
            List<Element<PartyFlagStructure>> updatedRespondent2Experts = updatePartyDQExperts(
                unwrapElements(caseData.getRespondent2Experts()),
                unwrapElements(mappedExperts)
            );
            builder.respondent2Experts(updatedRespondent2Experts);
        }

    }

    private void updateWitnesses(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        List<Element<UpdatePartyDetailsForm>> formData = caseData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm();
        List<Element<Witness>> mappedWitnesses;

        if (partyId.equals(CLAIMANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getApplicant1DQ().getApplicant1DQWitnesses(), formData);
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                     .applicant1DQWitnesses(
                                         caseData.getApplicant1DQ().getApplicant1DQWitnesses().toBuilder()
                                             .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
                                             .details(mappedWitnesses)
                                             .build())
                                     .build());
            List<Element<PartyFlagStructure>> updatedApplicantWitnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getApplicantWitnesses()),
                unwrapElements(mappedWitnesses)
            );
            builder.applicantWitnesses(updatedApplicantWitnesses);
        } else if (partyId.equals(DEFENDANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent1DQ().getRespondent1DQWitnesses(), formData);
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                 .respondent1DQWitnesses(
                                     caseData.getRespondent1DQ().getRespondent1DQWitnesses().toBuilder()
                                         .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
                                         .details(mappedWitnesses)
                                         .build())
                                 .build());
            List<Element<PartyFlagStructure>> updatedRespondent1Witnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getRespondent1Witnesses()),
                unwrapElements(mappedWitnesses)
            );
            builder.respondent1Witnesses(updatedRespondent1Witnesses);
        } else if (partyId.equals(DEFENDANT_TWO_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent2DQ().getRespondent2DQWitnesses(), formData);
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                 .respondent2DQWitnesses(
                                     caseData.getRespondent2DQ().getRespondent2DQWitnesses().toBuilder()
                                         .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
                                         .details(mappedWitnesses)
                                         .build())
                                 .build());
            List<Element<PartyFlagStructure>> updatedRespondent2Witnesses = updatePartyDQWitnesses(
                unwrapElements(caseData.getRespondent2Witnesses()),
                unwrapElements(mappedWitnesses)
            );
            builder.respondent2Witnesses(updatedRespondent2Witnesses);
        }
    }

    private void getFlagsForParty(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());

        // persist respondent flags (ccd issue)
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .flags(oldCaseData.getRespondent1().getFlags())
            .build();

        builder.respondent1(updatedRespondent1);

        // persist applicant flags (ccd issue)
        var updatedApplicant1 = caseData.getApplicant1().toBuilder()
            .flags(oldCaseData.getApplicant1().getFlags())
            .build();

        builder.applicant1(updatedApplicant1);

        // if present, persist the 2nd respondent flags in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(oldCaseData.getRespondent2()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .flags(oldCaseData.getRespondent2().getFlags())
                .build();

            builder.respondent2(updatedRespondent2);
        }

        // if present, persist the 2nd applicant flags in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getApplicant2()).isPresent()
            && ofNullable(oldCaseData.getApplicant2()).isPresent()) {
            var updatedApplicant2 = caseData.getApplicant2().toBuilder()
                .flags(oldCaseData.getApplicant2().getFlags())
                .build();

            builder.applicant2(updatedApplicant2);
        }
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Contact information changed"))
            .confirmationBody(format("### What happens next\nAny changes made to contact details have been updated in the Claim Details tab."))
            .build();
    }

    private boolean isBeforeAwaitingApplicantIntention(CaseData caseData) {
        return caseData.getCcdState().equals(AWAITING_APPLICANT_INTENTION) || caseData.getCcdState().equals(AWAITING_RESPONDENT_ACKNOWLEDGEMENT);
    }

    private boolean isAdmin(String userAuthToken) {
        return userService.getUserInfo(userAuthToken).getRoles()
            .stream().anyMatch(ADMIN_ROLES::contains);
    }
}
