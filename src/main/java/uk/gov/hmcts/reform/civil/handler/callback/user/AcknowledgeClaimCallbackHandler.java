package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
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
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ACKNOWLEDGE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
public class AcknowledgeClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ACKNOWLEDGE_CLAIM);

    public static final String CONFIRMATION_SUMMARY = "<br />You need to respond to the claim before %s."
        + "%n%n[Download the Acknowledgement of Claim form](%s)";
    public static final String ERROR_DEFENDANT_RESPONSE_SUBMITTED =
        "Defendant acknowledgement has already been recorded";

    private final ExitSurveyContentService exitSurveyContentService;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final StateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final UserService userService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateRespondent1Copy,
            callbackKey(V_1, ABOUT_TO_START), this::populateRespondentCopyObjects,
            callbackKey(MID, "confirm-details"), this::validateDateOfBirth,
            callbackKey(ABOUT_TO_SUBMIT), this::setNewResponseDeadlineV1,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::setNewResponseDeadlineV2,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //used in master
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
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1());

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            updatedCaseData.respondent2Copy(caseData.getRespondent2());
        }
        // Show error message if defendant tries to submit response again ONE_V_TWO_TWO_LEGAL_REP
        if (featureToggleService.isMultipartyEnabled()
            && (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1AcknowledgeNotificationDate() != null)
            || (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        if ((multiPartyScenario.equals(ONE_V_ONE) || multiPartyScenario.equals(TWO_V_ONE)
            || multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP))
            && caseData.getRespondent1AcknowledgeNotificationDate() != null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            isRespondent1 = NO;
        }
        updatedCaseData.isRespondent1(isRespondent1);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();

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

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
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

    //used in master
    private CallbackResponse setNewResponseDeadlineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
        LocalDateTime newResponseDate = deadlinesCalculator.plus14DaysAt4pmDeadline(responseDeadline);
        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();
        CaseData caseDataUpdated = caseData.toBuilder()
            .respondent1AcknowledgeNotificationDate(time.now())
            .respondent1ResponseDeadline(newResponseDate)
            .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
            .respondent1(updatedRespondent1)
            .respondent1Copy(null)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    private CallbackResponse setNewResponseDeadlineV2(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime respondent1ResponseDeadline = caseData.getRespondent1ResponseDeadline();
        LocalDateTime respondent2ResponseDeadline = caseData.getRespondent2ResponseDeadline();
        LocalDateTime newDeadlineRespondent1 = deadlinesCalculator.plus14DaysAt4pmDeadline(respondent1ResponseDeadline);
        LocalDateTime newDeadlineRespondent2 = deadlinesCalculator.plus14DaysAt4pmDeadline(respondent2ResponseDeadline);

        var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();

        CaseData.CaseDataBuilder caseDataUpdated = caseData.toBuilder();
        var respondent1Check = YES;
        if (solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            respondent1Check = NO;
        }

        /* for 1v1 */
        if (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(NO)
            && caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(NO)) {
            caseDataUpdated
                .respondent1AcknowledgeNotificationDate(time.now())
                .respondent1ResponseDeadline(newDeadlineRespondent1)
                .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
                .respondent1(updatedRespondent1)
                .respondent1Copy(null)
                .build();
        }
        //for 2v1
        if (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(YES)) {
            caseDataUpdated
                .respondent1AcknowledgeNotificationDate(time.now())
                .respondent1ResponseDeadline(newDeadlineRespondent1)
                .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
                .respondent1(updatedRespondent1)
                .respondent1Copy(null)
                .respondent1ClaimResponseIntentionType(caseData.getRespondent1ClaimResponseIntentionType())
                .respondent1ClaimResponseIntentionTypeApplicant2(
                    caseData.getRespondent1ClaimResponseIntentionTypeApplicant2())
                .build();
        } else if (caseData.getAddRespondent2() != null && caseData.getRespondent2() != null
            && respondent2HasSameLegalRep(caseData)) {
            //1v2 same
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            caseDataUpdated.respondent1AcknowledgeNotificationDate(time.now())
                .respondent1ResponseDeadline(newDeadlineRespondent1)
                .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
                .respondent1(updatedRespondent1)
                .respondent2(updatedRespondent2)
                .respondent1Copy(null)
                .respondent2Copy(null)
                .respondent1ClaimResponseIntentionType(caseData.getRespondent1ClaimResponseIntentionType())
                .respondent2ClaimResponseIntentionType(caseData.getRespondent2ClaimResponseIntentionType())
                .build();
        } else if (caseData.getRespondent1() != null && caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2().equals(YES)
            && respondent1Check.equals(YES) && !respondent2HasSameLegalRep(caseData)) {
            //1v2 diff login 1

            caseDataUpdated.respondent1AcknowledgeNotificationDate(time.now())
                .respondent1(updatedRespondent1)
                .respondent2(caseData.getRespondent2Copy())
                .respondent1ClaimResponseIntentionType(caseData.getRespondent1ClaimResponseIntentionType())
                .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
                .respondent1ResponseDeadline(newDeadlineRespondent1)
                .respondent1Copy(null)
                .isRespondent1(null);

        } else if (caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(YES)
            && respondent1Check.equals(NO) && !respondent2HasSameLegalRep(caseData)) {
            var updatedRespondent2 = caseData.getRespondent2Copy().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();
            //1v2 diff login 2
            caseDataUpdated
                .respondent2AcknowledgeNotificationDate(time.now())
                .respondent2(updatedRespondent2)
                .respondent1Copy(null)
                .respondent2Copy(null)
                .businessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM))
                .respondent2ResponseDeadline(newDeadlineRespondent2)
                .respondent2ClaimResponseIntentionType(caseData.getRespondent2ClaimResponseIntentionType())
                .isRespondent1(null)
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        LocalDateTime responseDeadline = isRespondent1(callbackParams)
            ? caseData.getRespondent1ResponseDeadline() : caseData.getRespondent2ResponseDeadline();

        String body = format(
            CONFIRMATION_SUMMARY,
            formatLocalDateTime(responseDeadline, DATE_TIME_AT),
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()))
            + exitSurveyContentService.respondentSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# You have acknowledged the claim%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
            .build();
    }

    private boolean isRespondent1(CallbackParams callbackParams) {
        return !solicitorRepresentsOnlyOneOfRespondents(callbackParams, RESPONDENTSOLICITORTWO);
    }
}
