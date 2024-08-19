package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.SetApplicantResponseDeadline;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.PopulateRespondentCopyObjects;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
@Slf4j
public class RespondToClaimCallbackHandler extends CallbackHandler implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE);

    private final ExitSurveyContentService exitSurveyContentService;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final LocationReferenceDataService locationRefDataService;
    private final PopulateRespondentCopyObjects populateRespondentCopyObjects;
    private final SetApplicantResponseDeadline setApplicantResponseDeadline;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateRespondentCopyObjects)
            .put(callbackKey(MID, "confirm-details"), this::validateDateOfBirth)
            .put(callbackKey(MID, "set-generic-response-type-flag"), this::setGenericResponseTypeFlag)
            .put(callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates)
            .put(callbackKey(MID, "experts"), this::validateRespondentExperts)
            .put(callbackKey(MID, "witnesses"), this::validateRespondentWitnesses)
            .put(callbackKey(MID, "upload"), this::emptyCallbackResponse)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse populateRespondentCopyObjects(CallbackParams callbackParams) {
        return populateRespondentCopyObjects.execute(callbackParams);
    }

    private boolean solicitorRepresentsOnlyOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private CallbackResponse validateRespondentWitnesses(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                return validateWitnesses(callbackParams.getCaseData().getRespondent1DQ());
            } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
                return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
            } else if (respondent2HasSameLegalRep(caseData)
                && (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO)
                && (caseData.getRespondent2DQ() != null
                && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null)) {
                return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
            }
        }
        return validateWitnesses(callbackParams.getCaseData().getRespondent1DQ());
    }

    private CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)) {
                return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
            } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
                return validateExperts(callbackParams.getCaseData().getRespondent2DQ());
            } else if (respondent2HasSameLegalRep(caseData)
                && (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO)
                && (caseData.getRespondent2DQ() != null
                && caseData.getRespondent2DQ().getRespondent2DQExperts() != null)) {
                return validateExperts(callbackParams.getCaseData().getRespondent2DQ());
            }
        }
        return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Hearing hearing = caseData.getRespondent1DQ().getHearing();

        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))
            && (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            || (respondent2HasSameLegalRep(caseData)
            && caseData.getRespondentResponseIsSame() != null
            && caseData.getRespondentResponseIsSame() == NO
            && caseData.getRespondent2DQ() != null
            && caseData.getRespondent2DQ().getHearing() != null))) {
            hearing = caseData.getRespondent2DQ().getHearing();
        }

        List<String> errors = unavailableDateValidator.validate(hearing);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        List<String> errors = dateOfBirthValidator.validate(respondent);

        ofNullable(callbackParams.getCaseData().getRespondent2())
            .ifPresent(party -> errors.addAll(dateOfBirthValidator.validate(party)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse setGenericResponseTypeFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData =
            caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            //1V2 Different Solicitors + Respondent 2 only
            isRespondent1 = NO;
        }

        if (isResponseMatchingType(caseData, isRespondent1, RespondentResponseType.FULL_DEFENCE)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE).build();
        } else if (isResponseMatchingType(caseData, isRespondent1, RespondentResponseType.PART_ADMISSION)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION).build();
        }

        List<String> errors = new ArrayList<>();
        if (isFullDefenceForBothDefendants(caseData) && respondent2HasSameLegalRep(caseData)) {
            errors.add(
                "It is not possible to respond for both defendants with Reject all of the claim. "
                    + "Please go back and select single response option."
            );
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse resetStatementOfTruth(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // resetting statement of truth field, this resets in the page, but the data is still sent to the db.
        // setting null here does not clear, need to overwrite with value.
        // must be to do with the way XUI cache data entered through the lifecycle of an event.
        CaseData updatedCaseData = caseData.toBuilder()
            .uiStatementOfTruth(StatementOfTruth.builder().role("").build())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse setApplicantResponseDeadline(CallbackParams callbackParams) {
        return setApplicantResponseDeadline.execute(callbackParams);

    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();
        String body;

        //catch scenario 1v2 Diff Sol - 1 Response Received
        //responseDeadline has not been set yet
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {
            body = "Once the other defendant's legal representative has submitted their defence, we will send the "
                + "claimant's legal representative a notification. You will receive a copy of this notification, "
                + "as it will include details of when the claimant must respond.";
        } else {
            LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
            body = format(
                "<br /> The Claimant legal representative will get a notification to confirm you have provided the "
                    + "Defendant defence. You will be CC'ed.%n"
                    + "The Claimant has until %s to discontinue or proceed with this claim",
                formatLocalDateTime(responseDeadline, DATE)
            ) + exitSurveyContentService.respondentSurvey();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(
                format("# You have submitted the Defendant's defence%n## Claim number: %s", claimNumber))
            .confirmationBody(body)
            .build();
    }

    private boolean isAwaitingAnotherDefendantResponse(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseType() == null
            || caseData.getRespondent2ClaimResponseType() == null;
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        return solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, caseRole);
    }

    private boolean isFullDefenceForBothDefendants(CaseData caseData) {
        return (caseData.getRespondent1ClaimResponseType() != null
            && caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE))
            && (caseData.getRespondent2ClaimResponseType() != null
            && caseData.getRespondent2ClaimResponseType().equals(
            RespondentResponseType.FULL_DEFENCE));
    }

    private boolean isResponseMatchingType(CaseData caseData, YesOrNo isRespondent1,
                                           RespondentResponseType type) {
        return isSolicitor1AndRespondent1ResponseIsMatchingType(caseData, isRespondent1, type)
            || isSolicitor2AndRespondent2ResponseIsMatchingType(caseData, isRespondent1, type)
            || isSameSolicitorAndAnyRespondentResponseIsMatchingType(caseData, type)
            || is2v1AndRespondent1ResponseIsMatchingTypeToAnyApplicant(caseData, type);
    }

    private boolean is2v1AndRespondent1ResponseIsMatchingTypeToAnyApplicant(CaseData caseData,
                                                                            RespondentResponseType type) {
        return TWO_V_ONE.equals(getMultiPartyScenario(caseData))
            && (type.equals(caseData.getRespondent1ClaimResponseType())
            || type.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
    }

    private boolean isSameSolicitorAndAnyRespondentResponseIsMatchingType(CaseData caseData,
                                                                          RespondentResponseType type) {
        return respondent2HasSameLegalRep(caseData)
            && (type.equals(caseData.getRespondent1ClaimResponseType())
            || type.equals(caseData.getRespondent2ClaimResponseType()));
    }

    private boolean isSolicitor2AndRespondent2ResponseIsMatchingType(CaseData caseData, YesOrNo isRespondent1,
                                                                     RespondentResponseType type) {
        return caseData.getRespondent2ClaimResponseType() != null
            && caseData.getRespondent2ClaimResponseType().equals(type)
            && isRespondent1.equals(NO);
    }

    private boolean isSolicitor1AndRespondent1ResponseIsMatchingType(CaseData caseData, YesOrNo isRespondent1,
                                                                     RespondentResponseType type) {
        return caseData.getRespondent1ClaimResponseType() != null
            && caseData.getRespondent1ClaimResponseType().equals(type)
            && isRespondent1.equals(YES);
    }
}
