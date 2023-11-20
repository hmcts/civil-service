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
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addApplicantExpertAndWitnessFlagsStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.addRespondentDQPartiesFlagStructure;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseNameInternal;
import static uk.gov.hmcts.reform.civil.utils.CaseNameUtils.buildCaseNamePublic;
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
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class ManageContactInformationCallbackHandler extends CallbackHandler {

    private static final String INVALID_CASE_STATE_ERROR = "You will be able run the manage contact information " +
        "event once the claimant has responded.";
    private static final String CHECK_LITIGATION_FRIEND_ERROR_TITLE = "Check the litigation friend's details";
    private static final String CHECK_LITIGATION_FRIEND_ERROR = "After making these changes, please ensure that the "
        + "litigation friend's contact information is also up to date.";
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
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitChanges)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse prepareEvent(CallbackParams callbackParams) {
        //TODO: 1v2DS/SS -> LR to show LR org 1/2 dependning on MP
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
        if (partyId.equals(CLAIMANT_ONE_WITNESSES_ID)) {
            return mapWitnessesToUpdatePartyDetailsForm(caseData.getApplicant1DQ().getWitnesses().getDetails());
        } else if (partyId.equals(DEFENDANT_ONE_WITNESSES_ID)) {
            return mapWitnessesToUpdatePartyDetailsForm(caseData.getRespondent1DQ().getWitnesses().getDetails());
        } else if (partyId.equals(DEFENDANT_TWO_WITNESSES_ID)) {
            return mapWitnessesToUpdatePartyDetailsForm(caseData.getRespondent2DQ().getWitnesses().getDetails());
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
        String partyChosen = caseData.getUpdateDetailsForm().getPartyChosen().getValue().getCode();

        updateExperts(caseData.getUpdateDetailsForm().getPartyChosenId(), caseData, builder);
        updateWitnesses(caseData.getUpdateDetailsForm().getPartyChosenId(), caseData, builder);

        if (isParty(partyChosen) || isLitigationFriend(partyChosen)) {
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
                caseData.getApplicant1DQ().getApplicant1DQExperts().getDetails(), formData);
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                     .applicant1DQExperts(
                                         caseData.getApplicant1DQ().getApplicant1DQExperts().toBuilder()
                                             .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
                                             .details(mappedExperts)
                                             .build())
                                     .build());
            addApplicantExpertAndWitnessFlagsStructure(builder, caseData);
            //TODO: need to add it to top level party object
        } else if (partyId.equals(DEFENDANT_ONE_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent1DQ().getRespondent1DQExperts().getDetails(), formData);
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                     .respondent1DQExperts(
                                         caseData.getRespondent1DQ().getRespondent1DQExperts().toBuilder()
                                             .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
                                             .details(mappedExperts)
                                             .build())
                                     .build());
            addRespondentDQPartiesFlagStructure(builder, caseData);
            //TODO: need to add it to top level party object
        } else if (partyId.equals(DEFENDANT_TWO_EXPERTS_ID)) {
            mappedExperts = mapUpdatePartyDetailsFormToDQExperts(
                caseData.getRespondent2DQ().getRespondent2DQExperts().getDetails(), formData);
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                     .respondent2DQExperts(
                                         caseData.getRespondent2DQ().getRespondent2DQExperts().toBuilder()
                                             .expertRequired(mappedExperts.size() >= 1 ? YES : NO)
                                             .details(mappedExperts)
                                             .build())
                                     .build());
            addRespondentDQPartiesFlagStructure(builder, caseData);
            //TODO: need to add it to top level party object
        }

    }

    private void updateWitnesses(String partyId, CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        List<Element<UpdatePartyDetailsForm>> formData = caseData.getUpdateDetailsForm().getUpdateWitnessesDetailsForm();
        List<Element<Witness>> mappedWitnesses;

        if (partyId.equals(CLAIMANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getApplicant1DQ().getApplicant1DQWitnesses().getDetails(), formData);
            builder.applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                     .applicant1DQWitnesses(
                                         caseData.getApplicant1DQ().getApplicant1DQWitnesses().toBuilder()
                                             .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
                                             .details(mappedWitnesses)
                                             .build())
                                     .build());
            addApplicantExpertAndWitnessFlagsStructure(builder, caseData);
            //TODO: need to add it to top level party object
        } else if (partyId.equals(DEFENDANT_ONE_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent1DQ().getRespondent1DQWitnesses().getDetails(), formData);
            builder.respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                 .respondent1DQWitnesses(
                                     caseData.getRespondent1DQ().getRespondent1DQWitnesses().toBuilder()
                                         .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
                                         .details(mappedWitnesses)
                                         .build())
                                 .build());
            addRespondentDQPartiesFlagStructure(builder, caseData);
            //TODO: need to add it to top level party object
        } else if (partyId.equals(DEFENDANT_TWO_WITNESSES_ID)) {
            mappedWitnesses = mapUpdatePartyDetailsFormToDQWitnesses(
                caseData.getRespondent2DQ().getRespondent2DQWitnesses().getDetails(), formData);
            builder.respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                                 .respondent2DQWitnesses(
                                     caseData.getRespondent2DQ().getRespondent2DQWitnesses().toBuilder()
                                         .witnessesToAppear(mappedWitnesses.size() >= 1 ? YES : NO)
                                         .details(mappedWitnesses)
                                         .build())
                                 .build());
            addRespondentDQPartiesFlagStructure(builder, caseData);
            //TODO: need to add it to top level party object
        }
    }

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
