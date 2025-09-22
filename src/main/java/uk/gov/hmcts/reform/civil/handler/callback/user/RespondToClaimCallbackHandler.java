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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.PopulateRespondentCopyObjects;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.SetApplicantResponseDeadline;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.ValidateRespondentExperts;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.ValidateRespondentWitnesses;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.ValidateUnavailableDates;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
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
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
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
    private final ObjectMapper objectMapper;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final PopulateRespondentCopyObjects populateRespondentCopyObjects;
    private final SetApplicantResponseDeadline setApplicantResponseDeadline;
    private final ValidateRespondentWitnesses validateRespondentWitnesses;
    private final ValidateRespondentExperts validateRespondentExperts;
    private final ValidateUnavailableDates validateUnavailableDates;

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

    private boolean isSolicitorRepresentingOnlyOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
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
        return validateRespondentWitnesses.execute(callbackParams);
    }

    private CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        return validateRespondentExperts.execute(callbackParams);

    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        return validateUnavailableDates.execute(callbackParams);

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

        YesOrNo isRespondent1 = YES;
        if (isSolicitorRepresentingOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            isRespondent1 = NO;
        }

        if (isResponseMatchingType(caseData, isRespondent1, RespondentResponseType.FULL_DEFENCE)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE).build();
        } else if (isResponseMatchingType(caseData, isRespondent1, RespondentResponseType.PART_ADMISSION)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.PART_ADMISSION).build();
        }

        List<String> errors = new ArrayList<>();
        if (isFullDefenceForBothDefendants(caseData) && isRespondent2SameLegalRep(caseData)) {
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
        log.info("Resetting statement of truth for Case ID: {}", caseData.getCcdCaseReference());
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

    private boolean isRespondent2SameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();
        String body;

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
        return isRespondent2SameLegalRep(caseData)
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
