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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;

@Service
@RequiredArgsConstructor
public class AcknowledgeClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ACKNOWLEDGE_CLAIM);
    private static final String RESPONDENT2_DOCUMENT_GENERATION_USER = "userRespondent2";

    public static final String CONFIRMATION_SUMMARY = "<br />You need to respond to the claim before %s."
        + "%n%n[Download the Acknowledgement of Claim form](%s)";
    public static final String ERROR_DEFENDANT_RESPONSE_SUBMITTED =
        "Defendant acknowledgement has already been recorded";

    private final ExitSurveyContentService exitSurveyContentService;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final IStateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;
    private final ObjectMapper objectMapper;
    private final Time time;
    private final UserService userService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateRespondentCopyObjects,
            callbackKey(MID, "confirm-details"), this::validateDateOfBirth,
            callbackKey(MID, "solicitor-reference"), this::populateSolicitorReferenceCopy,
            callbackKey(ABOUT_TO_SUBMIT), this::setNewResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateRespondentCopyObjects(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        caseData.setRespondent1Copy(caseData.getRespondent1());
        ofNullable(caseData.getRespondent2()).ifPresent(caseData::setRespondent2Copy);

        // Show an error message if the defendant tries to submit a response again ONE_V_TWO_TWO_LEGAL_REP
        if ((solicitorRepresentsOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1AcknowledgeNotificationDate() != null)
            || (solicitorRepresentsOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if ((List.of(ONE_V_ONE, TWO_V_ONE, ONE_V_TWO_ONE_LEGAL_REP).contains(multiPartyScenario))
            && caseData.getRespondent1AcknowledgeNotificationDate() != null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        var isRespondent1 = solicitorRepresentsOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO) ? NO : YES;
        caseData.setSolicitorReferencesCopy(caseData.getSolicitorReferences());
        caseData.setIsRespondent1(isRespondent1);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        List<String> errors = new ArrayList<>();
        ofNullable(callbackParams.getCaseData().getRespondent1())
            .ifPresent(party -> errors.addAll(dateOfBirthValidator.validate(party)));
        ofNullable(callbackParams.getCaseData().getRespondent2())
            .ifPresent(party -> errors.addAll(dateOfBirthValidator.validate(party)));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse populateSolicitorReferenceCopy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        caseData.setSolicitorReferencesCopy(caseData.getSolicitorReferences());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse setNewResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = getAuthenticatedUserInfo(callbackParams);

        applyCopiedRespondentAddresses(caseData);
        setRespondent2DocumentGenerationUser(caseData, userInfo);

        LocalDateTime respondent1NewDeadline = ofNullable(caseData.getRespondent1ResponseDeadline())
            .map(deadlinesCalculator::plus14DaysDeadline)
            .orElse(null);
        LocalDateTime respondent2NewDeadline = ofNullable(caseData.getRespondent2ResponseDeadline())
            .map(deadlinesCalculator::plus14DaysDeadline)
            .orElse(null);

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        boolean isRespondent2Solicitor = solicitorRepresentsOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO);

        // Each scenario owns a different acknowledgement/deadline update path.
        // Keep this switch aligned with getMultiPartyScenario(...) values to avoid partial updates.
        switch (multiPartyScenario) {
            case ONE_V_ONE, TWO_V_ONE -> applyOneVsOneOrTwoVsOneDeadlines(caseData, respondent1NewDeadline);
            case ONE_V_TWO_ONE_LEGAL_REP -> applyOneVsTwoSameRepDeadlines(caseData, respondent1NewDeadline);
            case ONE_V_TWO_TWO_LEGAL_REP -> applyOneVsTwoDifferentRepDeadlines(
                caseData, respondent1NewDeadline, respondent2NewDeadline, isRespondent2Solicitor
            );
            default -> throw new IllegalStateException("Unexpected value: " + multiPartyScenario);
        }

        applyCommonAcknowledgementFields(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        boolean isRespondent1Solicitor = !solicitorRepresentsOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO);
        LocalDateTime responseDeadline = isRespondent1Solicitor
            ? caseData.getRespondent1ResponseDeadline() : caseData.getRespondent2ResponseDeadline();

        String body = format(
            CONFIRMATION_SUMMARY,
            formatLocalDateTime(responseDeadline, DATE_TIME_AT),
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()))
            + exitSurveyContentService.respondentSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(
                "# You have acknowledged the claim%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
            .build();
    }

    private void applyCopiedRespondentAddresses(CaseData caseData) {
        if (caseData.getRespondent1Copy() != null) {
            caseData.getRespondent1().setPrimaryAddress(caseData.getRespondent1Copy().getPrimaryAddress());
        }
        if (caseData.getRespondent2() != null && caseData.getRespondent2Copy() != null) {
            caseData.getRespondent2().setPrimaryAddress(caseData.getRespondent2Copy().getPrimaryAddress());
        }
    }

    private void setRespondent2DocumentGenerationUser(CaseData caseData, UserInfo userInfo) {
        caseData.setRespondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference().toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            caseData.setRespondent2DocumentGeneration(RESPONDENT2_DOCUMENT_GENERATION_USER);
        }
    }

    private void applyOneVsOneOrTwoVsOneDeadlines(CaseData caseData, LocalDateTime newDeadline) {
        caseData.setRespondent1AcknowledgeNotificationDate(time.now());
        caseData.setRespondent1ResponseDeadline(newDeadline);
        caseData.setRespondent1(caseData.getRespondent1());
        caseData.setRespondent1Copy(null);
    }

    private void applyOneVsTwoSameRepDeadlines(CaseData caseData, LocalDateTime newDeadline) {
        caseData.setRespondent1AcknowledgeNotificationDate(time.now());
        caseData.setRespondent2AcknowledgeNotificationDate(time.now());
        caseData.setRespondent1ResponseDeadline(newDeadline);
        caseData.setRespondent2ResponseDeadline(newDeadline);
        caseData.setRespondent1(caseData.getRespondent1());
        caseData.setRespondent2(caseData.getRespondent2());
        caseData.setRespondent1Copy(null);
        caseData.setRespondent2Copy(null);
    }

    private void applyOneVsTwoDifferentRepDeadlines(CaseData caseData,
                                                    LocalDateTime respondent1NewDeadline,
                                                    LocalDateTime respondent2NewDeadline,
                                                    boolean isRespondent2Solicitor) {
        // In 1v2 with different reps, only the submitting solicitor's side is acknowledged here.
        if (!isRespondent2Solicitor) {
            caseData.setRespondent1AcknowledgeNotificationDate(time.now());
            caseData.setRespondent1ResponseDeadline(respondent1NewDeadline);
            caseData.setRespondent1(caseData.getRespondent1());
            caseData.setRespondent2(caseData.getRespondent2Copy());
        } else {
            caseData.setRespondent2AcknowledgeNotificationDate(time.now());
            caseData.setRespondent2ResponseDeadline(respondent2NewDeadline);
            caseData.setRespondent2(caseData.getRespondent2());
        }
        caseData.setSolicitorReferences(caseData.getSolicitorReferencesCopy());
        caseData.setRespondent1Copy(null);
        caseData.setRespondent2Copy(null);
        caseData.setIsRespondent1(null);
    }

    private void applyCommonAcknowledgementFields(CaseData caseData) {
        caseData.setBusinessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM));
        caseData.setSolicitorReferencesCopy(null);

        LocalDateTime respondent1Deadline = caseData.getRespondent1ResponseDeadline();
        LocalDateTime respondent2Deadline = caseData.getRespondent2ResponseDeadline();

        // 1v2 different reps can produce two active response deadlines; use the earlier one as the nextDeadline.
        if (getMultiPartyScenario(caseData) == MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) {
            caseData.setNextDeadline(deadlinesCalculator.nextDeadline(Arrays.asList(respondent1Deadline, respondent2Deadline)).toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(
                ofNullable(caseData.getSolicitorReferences()).map(SolicitorReferences::getRespondentSolicitor1Reference).orElse(null),
                caseData.getRespondentSolicitor2Reference()));
        } else {
            // Other scenarios have one effective respondent deadline at this stage.
            caseData.setNextDeadline(ofNullable(respondent1Deadline).orElse(respondent2Deadline).toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(caseData));
        }
    }

    private boolean solicitorRepresentsOneOrBothRespondents(CallbackParams callbackParams, CaseRole caseRole) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = getAuthenticatedUserInfo(callbackParams);
        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            caseRole
        );
    }

    private UserInfo getAuthenticatedUserInfo(CallbackParams callbackParams) {
        return userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
    }

}
