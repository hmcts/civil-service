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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.*;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
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
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
public class RespondToClaimCallbackHandler extends CallbackHandler implements ExpertsValidator, WitnessesValidator {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(DEFENDANT_RESPONSE);

    public static final String ERROR_DEFENDANT_RESPONSE_SUBMITTED =
        "There is a problem"
        + "\n"
        + "You have already submitted the defendant's response";

    private final ExitSurveyContentService exitSurveyContentService;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final UnavailableDateValidator unavailableDateValidator;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final DeadlinesCalculator deadlinesCalculator;
    private final StateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final UserService userService;
    private final FeatureToggleService featureToggleService;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateRespondent1Copy)
            .put(callbackKey(V_1, ABOUT_TO_START), this::populateRespondentCopyObjects)
            .put(callbackKey(MID, "confirm-details"), this::validateDateOfBirth)
            .put(callbackKey(MID, "set-generic-response-type-flag"), this::setGenericResponseTypeFlag)
            .put(callbackKey(MID, "validate-unavailable-dates"), this::validateUnavailableDates)
            .put(callbackKey(MID, "experts"), this::validateRespondentExperts)
            .put(callbackKey(MID, "witnesses"), this::validateRespondentWitnesses)
            .put(callbackKey(MID, "upload"), this::emptyCallbackResponse)
            .put(callbackKey(MID, "statement-of-truth"), this::resetStatementOfTruth)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::setApplicantResponseDeadline)
            .put(callbackKey(V_1, ABOUT_TO_SUBMIT), this::setApplicantResponseDeadlineV1)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    // currently used by master
    private CallbackResponse populateRespondent1Copy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse populateRespondentCopyObjects(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        // Show error message if defendant tries to submit response again
        if (featureToggleService.isMultipartyEnabled()) {
            if ((solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)
                && caseData.getRespondent1ResponseDate() != null)
                || (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
                && caseData.getRespondent2ResponseDate() != null)) {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                    .build();
            }
        }

        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedCaseData.respondent2Copy(caseData.getRespondent2());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateRespondentWitnesses(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isMultipartyEnabled()) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)){
                return validateWitnesses(callbackParams.getCaseData().getRespondent1DQ());
            } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)){
                return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
            } else if(respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if(caseData.getRespondent2DQ()!=null && caseData.getRespondent2DQ().getRespondent2DQWitnesses()!=null) {
                        return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
                    }
                }
            }
        }
        return validateWitnesses(callbackParams.getCaseData().getRespondent1DQ());
    }

    private CallbackResponse validateRespondentExperts(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isMultipartyEnabled()) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)){
                return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
            } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)){
                return validateExperts(callbackParams.getCaseData().getRespondent2DQ());
            } else if(respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if(caseData.getRespondent2DQ()!=null && caseData.getRespondent2DQ().getRespondent2DQExperts()!=null) {
                        return validateExperts(callbackParams.getCaseData().getRespondent2DQ());
                    }
                }
            }
        }
        return validateExperts(callbackParams.getCaseData().getRespondent1DQ());
    }

    private CallbackResponse validateUnavailableDates(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Hearing hearing = caseData.getRespondent1DQ().getHearing();

        if (featureToggleService.isMultipartyEnabled()) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)){
                hearing = caseData.getRespondent2DQ().getHearing();
            } else if(respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if(caseData.getRespondent2DQ()!=null && caseData.getRespondent2DQ().getHearing()!=null) {
                        hearing = caseData.getRespondent2DQ().getHearing();
                    }
                }
            }
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
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        if((caseData.getRespondent1ClaimResponseType() !=null && caseData.getRespondent1ClaimResponseType().equals(
            RespondentResponseType.FULL_DEFENCE))
        || (caseData.getRespondent2ClaimResponseType() !=null && caseData.getRespondent2ClaimResponseType().equals(
            RespondentResponseType.FULL_DEFENCE))){
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
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

    //currently used in master definition
    private CallbackResponse setApplicantResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();

        CaseData.CaseDataBuilder updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null)
            .respondent1ResponseDate(responseDate)
            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack))
            .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

        // moving statement of truth value to correct field, this was not possible in mid event.
        StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
        Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
            .respondent1DQStatementOfTruth(statementOfTruth)
            .build();

        updatedData.respondent1DQ(dq);
        // resetting statement of truth to make sure it's empty the next time it appears in the UI.
        updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    //TODO: 2nd DQ for 2nd solicitor
    //TODO: how best to store DQ when one solicitor representing both
    private CallbackResponse setApplicantResponseDeadlineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        // persist respondent address (ccd issue)
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();

        CaseData.CaseDataBuilder updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
        }

        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();

        // 1v2 same legal rep - will respond for both and set applicant 1 response deadline
        if (respondent2HasSameLegalRep(caseData)) {
            // if responses are marked as same, copy respondent 1 values into respondent 2
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
                updatedData
                    .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
                    .respondent1ResponseDate(responseDate)
                    .respondent2ResponseDate(responseDate)
                    .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));

                // moving statement of truth value to correct field, this was not possible in mid event.
                StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
                Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
                    .respondent1DQStatementOfTruth(statementOfTruth)
                    .build();

                updatedData.respondent1DQ(dq);
                // resetting statement of truth to make sure it's empty the next time it appears in the UI.
                updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
            } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                //if same solicitor but diversion. it checks DQ1 or/and DQ2 are populated.
                if(caseData.getRespondent1DQ()!=null
                    && caseData.getRespondent1ClaimResponseType()!=null
                    && caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)){
                    updatedData.respondent1ResponseDate(responseDate)
                        .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));


                    if ((caseData.getAddRespondent2() != null && caseData.getAddRespondent2() == NO)
                        || caseData.getRespondent2ResponseDate() != null
                        || (caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES)) {
                        updatedData
                            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));
                    }

                    // moving statement of truth value to correct field, this was not possible in mid event.
                    StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
                    Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
                        .respondent1DQStatementOfTruth(statementOfTruth)
                        .build();

                    updatedData.respondent1DQ(dq);
                    // resetting statement of truth to make sure it's empty the next time it appears in the UI.
                    updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
                }

                if(caseData.getRespondent2DQ()!=null
                    && caseData.getRespondent2ClaimResponseType()!=null
                    && caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)){
                    updatedData.respondent2ResponseDate(responseDate)
                        .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));


                    if (caseData.getRespondent1ResponseDate() != null) {
                        updatedData
                            .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));
                    }

                    // 1v1, 2v1
                    // represents 1st respondent - need to set deadline if only 1 respondent,
                    // or wait for 2nd respondent response before setting deadline
                    // moving statement of truth value to correct field, this was not possible in mid event.
                    StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
                    Respondent2DQ dq = caseData.getRespondent2DQ().toBuilder()
                        .respondent2DQStatementOfTruth(statementOfTruth)
                        .build();

                    updatedData.respondent2DQ(dq);
                    // resetting statement of truth to make sure it's empty the next time it appears in the UI.
                    updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
                }


            }

            // only represents 2nd respondent - need to wait for respondent 1 before setting applicant response deadline
        } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedData.respondent2ResponseDate(responseDate)
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

            if (caseData.getRespondent1ResponseDate() != null) {
                updatedData
                    .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));
            }

            // 1v1, 2v1
            // represents 1st respondent - need to set deadline if only 1 respondent,
            // or wait for 2nd respondent response before setting deadline
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent2DQ dq = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth)
                .build();

            updatedData.respondent2DQ(dq);
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        } else {
            updatedData.respondent1ResponseDate(responseDate)
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

            if ((caseData.getAddRespondent2() != null && caseData.getAddRespondent2() == NO)
                || caseData.getRespondent2ResponseDate() != null
                || (caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES)) {
                updatedData
                    .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));
            }

            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth)
                .build();

            updatedData.respondent1DQ(dq);
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        }

        if (featureToggleService.isMultipartyEnabled()
            && getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state("AWAITING_APPLICANT_INTENTION")
            .build();
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate, AllocatedTrack allocatedTrack) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate, allocatedTrack);
    }

    //TODO: find a workaround for applicant1respondentdeadline not being set in 1v2 diff sol case
    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();
        String body;

        //catch scenario 1v2 Diff Sol - 1 Response Received
        //responseDeadline has not been set yet
        if (featureToggleService.isMultipartyEnabled()
            && getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {
            body = "TBC";
        } else if (respondent2HasSameLegalRep(caseData)) {
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                body = "TBC";
            } else {
                body = "TBC";
            }
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

    private boolean isAwaitingDefendantOneResponse(CaseData caseData) {
        return caseData.getRespondent1ClaimResponseType() == null
            && caseData.getRespondent2ClaimResponseType() != null;
    }

    private boolean solicitorRepresentsOnlyOneOfRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }
}
