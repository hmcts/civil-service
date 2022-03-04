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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.CaseDocument;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.model.documents.DocumentType;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
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

        updatedCaseData.respondent1DetailsForClaimDetailsTab(caseData.getRespondent1());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedCaseData.respondent2Copy(caseData.getRespondent2());
            updatedCaseData.respondent2DetailsForClaimDetailsTab(caseData.getRespondent2());
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
        CaseData.CaseDataBuilder updatedData =
            caseData.toBuilder().multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.NOT_FULL_DEFENCE);

        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            //1V2 Different Solicitors + Respondent 2 only
            isRespondent1 = NO;
        }

        if (isSolicitor1AndRespondent1ResponseIsFullDefence(caseData, isRespondent1)
            || isSolicitor2AndRespondent2ResponseIsFullDefence(caseData, isRespondent1)
            || isSameSolicitorAndAnyRespondentResponseIsFullDefence(caseData)
            || is2v1AndRespondent1ResponseIsFullDefenceToAnyApplicant(caseData)) {
            updatedData.multiPartyResponseTypeFlags(MultiPartyResponseTypeFlags.FULL_DEFENCE)
                .build();
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

        // persist respondent address (ccd issue)
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();

        CaseData.CaseDataBuilder updatedData = caseData.toBuilder()
            .respondent1(updatedRespondent1)
            .respondent1Copy(null);

        updatedData.respondent1DetailsForClaimDetailsTab(updatedRespondent1);

        // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
        if (ofNullable(caseData.getRespondent2()).isPresent()
            && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            updatedData.respondent2(updatedRespondent2).respondent2Copy(null);
            updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2);
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
                //1v2 same solictor responding to respondents individually
            } else if (caseData.getRespondentResponseIsSame() != null && caseData.getRespondentResponseIsSame() == NO) {

                updatedData
                    .businessProcess(BusinessProcess.ready(DEFENDANT_RESPONSE))
                    .respondent1ResponseDate(responseDate)
                    .respondent2ResponseDate(responseDate)
                    .applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));

                StatementOfTruth statementOfTruth = caseData.getUiStatementOfTruth();
                if (caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {
                    // moving statement of truth value to correct field, this was not possible in mid event.
                    Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
                        .respondent1DQStatementOfTruth(statementOfTruth)
                        .build();

                    updatedData.respondent1DQ(dq);
                } else {
                    //required as ccd populated the respondent DQ with null objects.
                    updatedData.respondent1DQ(null);
                }

                if (caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)) {

                    Respondent2DQ dq2 = caseData.getRespondent2DQ().toBuilder()
                        .respondent2DQStatementOfTruth(statementOfTruth)
                        .build();

                    updatedData.respondent2DQ(dq2);
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

            if (respondent2NotPresent(caseData)
                || applicant2Present(caseData)
                || caseData.getRespondent2ResponseDate() != null) {
                updatedData.applicant1ResponseDeadline(getApplicant1ResponseDeadline(responseDate, allocatedTrack));
            }
            // if present, persist the 2nd respondent address in the same fashion as above, i.e ignore for 1v1
            if (ofNullable(caseData.getRespondent2()).isPresent()
                && ofNullable(caseData.getRespondent2Copy()).isPresent()) {
                var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                    .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                    .build();

                updatedData
                    .respondent2(updatedRespondent2)
                    .respondent2Copy(null);
                updatedData.respondent2DetailsForClaimDetailsTab(updatedRespondent2);
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
            Respondent1DQ dq = caseData.getRespondent1DQ().toBuilder()
                .respondent1DQStatementOfTruth(statementOfTruth)
                .build();

            updatedData.respondent1DQ(dq);
            // resetting statement of truth to make sure it's empty the next time it appears in the UI.
            updatedData.uiStatementOfTruth(StatementOfTruth.builder().build());
        }
        updatedData.isRespondent1(null);
        assembleResponseDocuments(caseData, updatedData);
        if (getMultiPartyScenario(caseData) == ONE_V_TWO_TWO_LEGAL_REP
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

    private void assembleResponseDocuments(CaseData caseData, CaseData.CaseDataBuilder updatedCaseData) {
        List<Element<CaseDocument>> defendantUploads = new ArrayList<>();
        Optional.ofNullable(caseData.getRespondent1ClaimResponseDocument())
            .map(ResponseDocument::getFile).ifPresent(respondent1ClaimDocument -> defendantUploads.add(
                buildElemCaseDocument(respondent1ClaimDocument, "Defendant",
                                      caseData.getRespondent1ResponseDate(), DocumentType.DEFENDANT_DEFENCE
                )));
        Optional.ofNullable(caseData.getRespondent1DQ())
            .map(Respondent1DQ::getRespondent1DQDraftDirections)
            .ifPresent(respondent1DQ -> defendantUploads.add(
                buildElemCaseDocument(respondent1DQ, "Defendant",
                                      caseData.getRespondent1ResponseDate(), DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                )));
        Optional.ofNullable(caseData.getRespondent2ClaimResponseDocument())
            .map(ResponseDocument::getFile).ifPresent(respondent2ClaimDocument -> defendantUploads.add(
                buildElemCaseDocument(respondent2ClaimDocument, "Defendant 2",
                                      caseData.getRespondent2ResponseDate(), DocumentType.DEFENDANT_DEFENCE
                )));
        Optional.ofNullable(caseData.getRespondent2DQ())
            .map(Respondent2DQ::getRespondent2DQDraftDirections)
            .ifPresent(respondent2DQ -> defendantUploads.add(
                buildElemCaseDocument(respondent2DQ, "Defendant 2",
                                      caseData.getRespondent2ResponseDate(), DocumentType.DEFENDANT_DRAFT_DIRECTIONS
                )));
        if (!defendantUploads.isEmpty()) {
            updatedCaseData.defendantResponseDocuments(defendantUploads);
        }
    }

    private Element<CaseDocument> buildElemCaseDocument(Document document, String createdBy,
                                                        LocalDateTime createdAt, DocumentType type) {
        return ElementUtils.element(uk.gov.hmcts.reform.civil.model.documents.CaseDocument.builder()
                       .documentLink(document)
                       .documentName(document.getDocumentFileName())
                       .documentType(type)
                       .createdDatetime(createdAt)
                       .createdBy(createdBy)
                       .build()
                );
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

    private boolean is2v1AndRespondent1ResponseIsFullDefenceToAnyApplicant(CaseData caseData) {
        return TWO_V_ONE.equals(getMultiPartyScenario(caseData))
            && (RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseTypeToApplicant2()));
    }

    private boolean isSameSolicitorAndAnyRespondentResponseIsFullDefence(CaseData caseData) {
        return respondent2HasSameLegalRep(caseData)
            && (RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent1ClaimResponseType())
            || RespondentResponseType.FULL_DEFENCE.equals(caseData.getRespondent2ClaimResponseType()));
    }

    private boolean isSolicitor2AndRespondent2ResponseIsFullDefence(CaseData caseData, YesOrNo isRespondent1) {
        return caseData.getRespondent2ClaimResponseType() != null
            && caseData.getRespondent2ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
            && isRespondent1.equals(NO);
    }

    private boolean isSolicitor1AndRespondent1ResponseIsFullDefence(CaseData caseData, YesOrNo isRespondent1) {
        return caseData.getRespondent1ClaimResponseType() != null
            && caseData.getRespondent1ClaimResponseType().equals(RespondentResponseType.FULL_DEFENCE)
            && isRespondent1.equals(YES);
    }
}
