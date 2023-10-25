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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.ExpertsValidator;
import uk.gov.hmcts.reform.civil.validation.interfaces.WitnessesValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.buildElemCaseDocument;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToRespondentExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToRespondentWitnesses;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
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
    private final LocationRefDataService locationRefDataService;
    private final CourtLocationUtils courtLocationUtils;
    private final FeatureToggleService toggleService;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final AssignCategoryId assignCategoryId;

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
        var caseData = callbackParams.getCaseData();
        LocalDateTime dateTime = LocalDateTime.now();

        // Show error message if defendant tries to submit response again
        if ((solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1ResponseDate() != null)
            || (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2ResponseDate() != null)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        //Show error message if defendant tries to submit a response after deadline has passed
        var respondent1ResponseDeadline = caseData.getRespondent1ResponseDeadline();
        var respondent2ResponseDeadline = caseData.getRespondent2ResponseDeadline();

        if ((solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1ResponseDate() == null
            && respondent1ResponseDeadline != null
            && dateTime.toLocalDate().isAfter(respondent1ResponseDeadline.toLocalDate()))
            || (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2ResponseDate() == null
            && respondent2ResponseDeadline != null
            && dateTime.toLocalDate().isAfter(respondent2ResponseDeadline.toLocalDate()))) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("You cannot submit a response now as you have passed your deadline"))
                .build();
        }

        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            //1V2 Different Solicitors + Respondent 2 only
            isRespondent1 = NO;
        }

        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .isRespondent1(isRespondent1);

        List<LocationRefData> locations = fetchLocationData(callbackParams);
        DynamicList courtLocationList = courtLocationUtils.getLocationsFromList(locations);
        RequestedCourt.RequestedCourtBuilder requestedCourt1 = RequestedCourt.builder();
        requestedCourt1.responseCourtLocations(courtLocationList);

        Optional.ofNullable(caseData.getCourtLocation())
            .map(CourtLocation::getApplicantPreferredCourt)
            .flatMap(applicantCourt -> locations.stream()
                .filter(locationRefData -> applicantCourt.equals(locationRefData.getCourtLocationCode()))
                .findFirst())
            .ifPresent(locationRefData -> requestedCourt1
                .otherPartyPreferredSite(locationRefData.getCourtLocationCode()
                                             + " " + locationRefData.getSiteName()));

        updatedCaseData
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(
                                   requestedCourt1
                                       .build())
                               .build());

        if (caseData.getRespondent2() != null) {
            updatedCaseData.respondent2DQ(
                Respondent2DQ.builder()
                    .respondent2DQRequestedCourt(requestedCourt1.build()).build());
        }

        updatedCaseData.respondent1DetailsForClaimDetailsTab(updatedCaseData.build().getRespondent1()
                                                                 .toBuilder().flags(null).build());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedCaseData
                .respondent2Copy(caseData.getRespondent2())
                .respondent2DetailsForClaimDetailsTab(updatedCaseData.build().getRespondent2()
                                                          .toBuilder().flags(null).build());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
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
            } else if (respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if (caseData.getRespondent2DQ() != null
                        && caseData.getRespondent2DQ().getRespondent2DQWitnesses() != null) {
                        return validateWitnesses(callbackParams.getCaseData().getRespondent2DQ());
                    }
                }
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
            } else if (respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if (caseData.getRespondent2DQ() != null
                        && caseData.getRespondent2DQ().getRespondent2DQExperts() != null) {
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

        if (!ONE_V_ONE.equals(MultiPartyScenario.getMultiPartyScenario(caseData))) {
            if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
                hearing = caseData.getRespondent2DQ().getHearing();
            } else if (respondent2HasSameLegalRep(caseData)) {
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {
                    if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null) {
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
        CaseData caseData = callbackParams.getCaseData();

        if (ofNullable(caseData.getRespondent1Copy()).isPresent()) {
            if (caseData.getRespondent1Copy().getPrimaryAddress() == null) {
                throw new IllegalArgumentException("Primary Address cannot be empty");
            }
        }

        // persist respondent address (ccd issue)
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .flags(caseData.getRespondent1Copy().getFlags())
            .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .flags(caseData.getRespondent2Copy().getFlags())
                .build();

            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
        }

        LocalDateTime responseDate = time.now();
        AllocatedTrack allocatedTrack = caseData.getAllocatedTrack();
        LocalDateTime applicant1Deadline = getApplicant1ResponseDeadline(responseDate, allocatedTrack);

        // 1v2 same legal rep - will respond for both and set applicant 1 response deadline
        if (respondent2HasSameLegalRep(caseData)) {
            // if responses are marked as same, copy respondent 1 values into respondent 2
            if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
                updatedData
                    .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
                    .respondent1ResponseDate(responseDate)
                    .respondent2ResponseDate(responseDate)
                    .nextDeadline(applicant1Deadline.toLocalDate())
                    .applicant1ResponseDeadline(applicant1Deadline);

                // moving statement of truth value to correct field, this was not possible in mid event.
                StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
                Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                    .respondent1DQStatementOfTruth(statementOfTruth);

                handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);

                updatedData.respondent1DQ(dq.build());

                // resetting statement of truth to make sure it's empty the next time it appears in the UI.
                updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
                //1v2 same Solicitor responding to respondents individually
            } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {

                updatedData
                    .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
                    .respondent1ResponseDate(responseDate)
                    .respondent2ResponseDate(responseDate)
                    .nextDeadline(applicant1Deadline.toLocalDate())
                    .applicant1ResponseDeadline(applicant1Deadline);

                StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
                if (caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                    // moving statement of truth value to correct field, this was not possible in mid event.
                    Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                        .respondent1DQStatementOfTruth(statementOfTruth);
                    handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
                    updatedData.respondent1DQ(dq.build());

                } else {
                    //required as ccd populated the respondent DQ with null objects.
                    updatedData.respondent1DQ(null);
                }

                if (caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {

                    Respondent2DQ.Respondent2DQBuilder dq2 = caseData.getRespondent2DQ().toBuilder()
                        .respondent2DQStatementOfTruth(statementOfTruth);
                    handleCourtLocationForRespondent2DQ(caseData, dq2, callbackParams);
                    updatedData.respondent2DQ(dq2.build());

                } else {
                    updatedData.respondent2DQ(null);
                }

                // resetting statement of truth to make sure it's empty the next time it appears in the UI.
                updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());

            }

            // only represents 2nd respondent - need to wait for respondent 1 before setting applicant response deadline
        } else if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            updatedData.respondent2ResponseDate(responseDate)
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

            if (caseData.getRespondent1ResponseDate() != null) {
                updatedData
                    .nextDeadline(applicant1Deadline.toLocalDate())
                    .applicant1ResponseDeadline(applicant1Deadline);
            } else {
                updatedData.nextDeadline(caseData.getRespondent1ResponseDeadline().toLocalDate());
            }

            // 1v1, 2v1
            // represents 1st respondent - need to set deadline if only 1 respondent,
            // or wait for 2nd respondent response before setting deadline
            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent2DQ.Respondent2DQBuilder dq = caseData.getRespondent2DQ().toBuilder()
                .respondent2DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent2DQ(caseData, dq, callbackParams);
            updatedData.respondent2DQ(dq.build());

            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        } else {
            updatedData.respondent1ResponseDate(responseDate)
                .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE));

            if (respondent2NotPresent(caseData)
                || applicant2Present(caseData)
                || caseData.getRespondent2ResponseDate() != null) {
                updatedData
                    .applicant1ResponseDeadline(applicant1Deadline)
                    .nextDeadline(applicant1Deadline.toLocalDate());
            }
            // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
            if (ofNullable(caseData.getRespondent2()).isPresent()
                && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
                var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                    .build();

                updatedData
                    .respondent2(updatedRespondent2)
                    .respondent2Copy(null)
                    .respondent2DetailsForClaimDetailsTab(updatedRespondent2.toBuilder().flags(null).build());

                if (caseData.getRespondent2ResponseDate() == null) {
                    updatedData.nextDeadline(caseData.getRespondent2ResponseDeadline().toLocalDate());
                }
            }

            // same legal rep - will respond for both and set applicant 1 response deadline
            if (respondent2HasSameLegalRep(caseData)) {
                // if responses are marked as same, copy respondent 1 values into respondent 2
                if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == YES) {
                    updatedData.respondent2ClaimResponseType(caseData.getRespondent1ClaimResponseType());
                }

                updatedData.respondent2ResponseDate(responseDate);
            }

            // moving statement of truth value to correct field, this was not possible in mid event.
            StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
            Respondent1DQ.Respondent1DQBuilder dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth);
            handleCourtLocationForRespondent1DQ(caseData, dq, callbackParams);
            updatedData.respondent1DQ(dq.build());
            log.info("handleCourtLocationForRespondent1DQ  "+ dq.build().toString());
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        }
        updatedData.isRespondent1(null);
        assembleResponseDocuments(caseData, updatedData);

        if (toggleService.isUpdateContactDetailsEnabled()) {
            addEventAndDateAddedToRespondentExperts(updatedData);
            addEventAndDateAddedToRespondentWitnesses(updatedData);
        }

        retainSolicitorReferences(callbackParams.getRequest().getCaseDetailsBefore().getData(), updatedData, caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForRespondent(updatedData,
                                                                        toggleService.isUpdateContactDetailsEnabled());

        updatedData.respondent1DetailsForClaimDetailsTab(updatedData.build().getRespondent1().toBuilder().flags(null).build());
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedData.respondent2DetailsForClaimDetailsTab(updatedData.build().getRespondent2().toBuilder().flags(null).build());
        }

        if (toggleService.isHmcEnabled()) {
            populateDQPartyIds(updatedData);
        }

        caseFlagsInitialiser.initialiseCaseFlags(DEFENDANT_RESPONSE, updatedData);

        if (toggleService.isCaseFileViewEnabled()) {
            // casefileview changes need to assign documents into specific folders, this is help determine
            // which user is "creating" the document and therefore which folder to move the documents
            // into, when directions order is generated in GenerateDirectionsQuestionnaireCallbackHandler
            UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
            updatedData.respondent2DocumentGeneration(null);
            if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                         .toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
                && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                           .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
                updatedData.respondent2DocumentGeneration("userRespondent2");
            }
        }

        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
            && isAwaitingAnotherDefendantResponse(caseData)) {

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.build().toMap(objectMapper))
                .build();
        }

        log.info("respondent 1 before nulling " + caseData.getRespondent1DQ());
        if (caseData.getRespondent2DQ() != null) {
            log.info("respondent 2 before nulling " + caseData.getRespondent2DQ());
        }

        // these documents are added to defendantUploads, if we do not remove/null the original,
        // case file view will show duplicate documents
        if (toggleService.isCaseFileViewEnabled()) {
            updatedData.respondent1ClaimResponseDocument(null);
            updatedData.respondent2ClaimResponseDocument(null);
            Respondent1DQ currentRespondent1DQ = caseData.getRespondent1DQ();
            currentRespondent1DQ.setRespondent1DQDraftDirections(null);
            updatedData.respondent1DQ(currentRespondent1DQ);
            Respondent2DQ currentRespondent2DQ = caseData.getRespondent2DQ();
            currentRespondent2DQ.setRespondent2DQDraftDirections(null);
            updatedData.respondent2DQ(currentRespondent2DQ);
        }
        log.info("respondent 1 After nulling " + caseData.getRespondent1DQ());
        if (caseData.getRespondent2DQ() != null) {
            log.info("respondent 2 after nulling " + caseData.getRespondent2DQ());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .state("AWAITING_APPLICANT_INTENTION")
            .build();
    }

    private void retainSolicitorReferences(Map<String, Object> beforeCaseData,
                                           CaseData.CaseDataBuilder<?, ?> updatedData,
                                           CaseData caseData) {

        @SuppressWarnings("unchecked")
        Map<String, String> solicitorRefs = ofNullable(beforeCaseData.get("solicitorReferences"))
            .map(refs -> objectMapper.convertValue(refs, HashMap.class))
            .orElse(null);
        SolicitorReferences solicitorReferences = ofNullable(solicitorRefs)
            .map(refMap -> {

                // collect data from recent form - defendantSolicitorRef1
                String defendantSolicitorRef1 = null;
                if (caseData.getSolicitorReferences() != null
                    && caseData.getSolicitorReferences().getRespondentSolicitor1Reference() != null) {
                    defendantSolicitorRef1 = caseData.getSolicitorReferences().getRespondentSolicitor1Reference();
                }

                return SolicitorReferences.builder()
                    .applicantSolicitor1Reference(
                        refMap.getOrDefault("applicantSolicitor1Reference", null))
                    // if solicitor reference recently changed in defendant response then use defendantSolicitorRef1
                    // else use data before it's updated
                    .respondentSolicitor1Reference(
                        ofNullable(defendantSolicitorRef1)
                            .orElse(refMap.getOrDefault("respondentSolicitor1Reference", null)))
                    .respondentSolicitor2Reference(
                        refMap.getOrDefault("respondentSolicitor2Reference", null))
                    .build();
            })
            .orElse(null);

        updatedData.solicitorReferences(solicitorReferences);

        String respondentSolicitor2Reference = ofNullable(caseData.getRespondentSolicitor2Reference())
            .orElse(ofNullable(beforeCaseData.get("respondentSolicitor2Reference"))
                        .map(Object::toString).orElse(null));

        updatedData
            .solicitorReferences(solicitorReferences)
            .respondentSolicitor2Reference(respondentSolicitor2Reference)
            .caseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(
                solicitorReferences != null ? ofNullable(solicitorReferences.getRespondentSolicitor1Reference())
                    .map(Object::toString).orElse(null) : null,
                respondentSolicitor2Reference
            ));
    }

    private void assembleResponseDocuments(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        ResponseDocument respondent1ClaimResponseDocument = caseData.getRespondent1ClaimResponseDocument();
        if (respondent1ClaimResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1ClaimDocument = respondent1ClaimResponseDocument.getFile();
            if (respondent1ClaimDocument != null) {
                Element<CaseDocument> documentElement =
                        buildElemCaseDocument(respondent1ClaimDocument, "Defendant",
                        updatedCaseData.build().getRespondent1ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                );
                assignCategoryId.assignCategoryIdToDocument(respondent1ClaimDocument,
                        DocCategory.DEF1_DEFENSE_DQ.getValue());
                CaseDocument copy = assignCategoryId
                        .copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF1.getValue());
                defendantUploads.add(documentElement);
                if (Objects.nonNull(copy)) {
                    defendantUploads.add(ElementUtils.element(copy));
                }
            }
        }

        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        if (respondent1DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent1DQDraftDirections = respondent1DQ.getRespondent1DQDraftDirections();
            if (respondent1DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent1DQDraftDirections,
                        "Defendant",
                        updatedCaseData.build().getRespondent1ResponseDate(),
                        DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                assignCategoryId.assignCategoryIdToDocument(respondent1DQDraftDirections,
                        DocCategory.DEF1_DEFENSE_DQ.getValue());
                CaseDocument copy = assignCategoryId
                        .copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF1.getValue());
                defendantUploads.add(documentElement);
                if (Objects.nonNull(copy)) {
                    defendantUploads.add(ElementUtils.element(copy));
                }
            }
        }

        ResponseDocument respondent2ClaimResponseDocument = caseData.getRespondent2ClaimResponseDocument();
        if (respondent2ClaimResponseDocument != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2ClaimDocument = respondent2ClaimResponseDocument.getFile();
            if (respondent2ClaimDocument != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2ClaimDocument, "Defendant 2",
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DEFENCE
                );
                CaseDocument copy = assignCategoryId
                        .copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF2.getValue());
                assignCategoryId.assignCategoryIdToDocument(respondent2ClaimDocument,
                        DocCategory.DEF2_DEFENSE_DQ.getValue());
                defendantUploads.add(documentElement);
                if (Objects.nonNull(copy)) {
                    defendantUploads.add(ElementUtils.element(copy));
                }
            }
        }
        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        if (respondent2DQ != null) {
            uk.gov.hmcts.reform.civil.documentmanagement.model.Document respondent2DQDraftDirections = respondent2DQ.getRespondent2DQDraftDirections();
            if (respondent2DQDraftDirections != null) {
                Element<CaseDocument> documentElement = buildElemCaseDocument(
                        respondent2DQDraftDirections,
                        "Defendant 2",
                        updatedCaseData.build().getRespondent2ResponseDate(),
                        DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                );
                CaseDocument copy = assignCategoryId
                        .copyCaseDocumentWithCategoryId(documentElement.getValue(), DocCategory.DQ_DEF2.getValue());
                assignCategoryId.assignCategoryIdToDocument(respondent2DQDraftDirections,
                        DocCategory.DEF2_DEFENSE_DQ.getValue());
                defendantUploads.add(documentElement);
                if (Objects.nonNull(copy)) {
                    defendantUploads.add(ElementUtils.element(copy));
                }
            }
        }

        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
    }

    private boolean applicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private boolean respondent2NotPresent(CaseData caseData) {
        return caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2() == NO;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }

    private LocalDateTime getApplicant1ResponseDeadline(LocalDateTime responseDate, AllocatedTrack allocatedTrack) {
        return deadlinesCalculator.calculateApplicantResponseDeadline(responseDate, allocatedTrack);
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
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private boolean isFullDefenceForBothDefendants(CaseData caseData) {
        if ((caseData.getRespondent1ClaimResponseType() != null
            && caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE))
            && (caseData.getRespondent2ClaimResponseType() != null
            && caseData.getRespondent2ClaimResponseType().equals(
            RespondentResponseType.FULL_DEFENCE))) {
            return true;
        }
        return false;
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

    private List<LocationRefData> fetchLocationData(CallbackParams callbackParams) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        return locationRefDataService.getCourtLocationsForDefaultJudgments(authToken);
    }

    private void handleCourtLocationForRespondent1DQ(CaseData caseData, Respondent1DQ.Respondent1DQBuilder dq,
                                                     CallbackParams callbackParams) {
        // data for court location
        if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations)
            .map(DynamicList::getValue).isPresent()) {
            DynamicList courtLocations = caseData
                .getRespondent1DQ().getRespondent1DQRequestedCourt().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), courtLocations);
            RequestedCourt.RequestedCourtBuilder dqBuilder = caseData.getRespondent1DQ()
                .getRespondent1DQRequestedCourt().toBuilder()
                .responseCourtLocations(null)
                .responseCourtCode(Optional.ofNullable(courtLocation)
                                       .map(LocationRefData::getCourtLocationCode)
                                       .orElse(caseData.getRespondent1DQ().getRespondent1DQRequestedCourt()
                                                   .getResponseCourtCode()));
            buildWithMatching(courtLocation).ifPresent(dqBuilder::caseLocation);
            dq.respondent1DQRequestedCourt(dqBuilder.build());
        } else if (Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations).isPresent()) {
            dq.respondent1DQRequestedCourt(caseData.getRespondent1DQ()
                                               .getRespondent1DQRequestedCourt()
                                               .toBuilder().responseCourtLocations(null).build());
        }
    }

    private void handleCourtLocationForRespondent2DQ(CaseData caseData, Respondent2DQ.Respondent2DQBuilder dq,
                                                     CallbackParams callbackParams) {
        // data for court location
        if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations)
            .map(DynamicList::getValue).isPresent()) {
            DynamicList courtLocations = caseData
                .getRespondent2DQ().getRespondent2DQRequestedCourt().getResponseCourtLocations();
            LocationRefData courtLocation = courtLocationUtils.findPreferredLocationData(
                fetchLocationData(callbackParams), courtLocations);
            RequestedCourt.RequestedCourtBuilder dqBuilder = caseData.getRespondent2DQ().getRequestedCourt()
                .toBuilder()
                .responseCourtLocations(null)
                .responseCourtCode(Optional.ofNullable(courtLocation)
                                       .map(LocationRefData::getCourtLocationCode)
                                       .orElse(caseData.getRespondent2DQ().getRespondent2DQRequestedCourt()
                                                   .getResponseCourtCode()));
            buildWithMatching(courtLocation).ifPresent(dqBuilder::caseLocation);
            dq.respondent2DQRequestedCourt(dqBuilder.build());
        } else if (Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQRequestedCourt)
            .map(RequestedCourt::getResponseCourtLocations).isPresent()) {
            dq.respondent2DQRequestedCourt(caseData.getRespondent2DQ()
                                               .getRespondent2DQRequestedCourt()
                                               .toBuilder().responseCourtLocations(null).build());
        }
    }

    private Optional<CaseLocationCivil> buildWithMatching(LocationRefData courtLocation) {
        return Optional.ofNullable(courtLocation).map(LocationHelper::buildCaseLocation);
    }

}
