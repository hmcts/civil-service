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
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
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
    private final FeatureToggleService featureToggleService;

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
        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            caseData.setRespondent2Copy(caseData.getRespondent2());
        }

        // Show error message if defendant tries to submit response again ONE_V_TWO_TWO_LEGAL_REP
        if ((solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORONE)
            && caseData.getRespondent1AcknowledgeNotificationDate() != null)
            || (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)
            && caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
        if ((multiPartyScenario.equals(ONE_V_ONE) || multiPartyScenario.equals(TWO_V_ONE)
            || multiPartyScenario.equals(ONE_V_TWO_ONE_LEGAL_REP))
            && caseData.getRespondent1AcknowledgeNotificationDate() != null) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of(ERROR_DEFENDANT_RESPONSE_SUBMITTED))
                .build();
        }

        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            isRespondent1 = NO;
        }
        caseData.setSolicitorReferencesCopy(caseData.getSolicitorReferences());
        caseData.setIsRespondent1(isRespondent1);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();

    }

    private CallbackResponse populateSolicitorReferenceCopy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        caseData.setSolicitorReferencesCopy(caseData.getSolicitorReferences());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
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

    private CallbackResponse setNewResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime respondent1ResponseDeadline = caseData.getRespondent1ResponseDeadline();
        LocalDateTime respondent2ResponseDeadline = caseData.getRespondent2ResponseDeadline();

        var solicitorReferencesCopy = caseData.getSolicitorReferencesCopy();
        var respondentSolicitor2Reference = caseData.getRespondentSolicitor2Reference();
        String defaultCaseListReferences = getAllDefendantSolicitorReferences(caseData);
        String solicitor1ReferenceFromCopy = solicitorReferencesCopy != null
            ? solicitorReferencesCopy.getRespondentSolicitor1Reference() : null;

        final var updatedRespondent1 = caseData.getRespondent1().toBuilder()
            .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
            .build();

        // casefileview changes need to assign documents into specific folders, this is help determine
        // which user is "creating" the document and therefore which folder to move the documents
        // into, when document is generated in GenerateAcknowledgementOfClaimCallbackHandler
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        caseData.setRespondent2DocumentGeneration(null);
        if (!coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                     .toString(), userInfo.getUid(), RESPONDENTSOLICITORONE)
            && coreCaseUserService.userHasCaseRole(caseData.getCcdCaseReference()
                                                       .toString(), userInfo.getUid(), RESPONDENTSOLICITORTWO)) {
            caseData.setRespondent2DocumentGeneration("userRespondent2");
        }

        LocalDateTime newDeadlineRespondent1 = deadlinesCalculator.plus14DaysDeadline(respondent1ResponseDeadline);
        LocalDateTime newDeadlineRespondent2 = null;

        var respondent1Check = YES;
        if (solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO)) {
            respondent1Check = NO;
            newDeadlineRespondent2 = deadlinesCalculator.plus14DaysDeadline(respondent2ResponseDeadline);
        }

        /* for 1v1 */
        if (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(NO)
            && caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(NO)) {
            caseData.setRespondent1AcknowledgeNotificationDate(time.now());
            caseData.setRespondent1ResponseDeadline(newDeadlineRespondent1);
            caseData.setBusinessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM));
            caseData.setRespondent1(updatedRespondent1);
            caseData.setRespondent1Copy(null);
            caseData.setSolicitorReferencesCopy(null);
            caseData.setNextDeadline(newDeadlineRespondent1.toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(defaultCaseListReferences);
        }
        //for 2v1
        if (caseData.getAddApplicant2() != null && caseData.getAddApplicant2().equals(YES)) {
            caseData.setRespondent1AcknowledgeNotificationDate(time.now());
            caseData.setRespondent1ResponseDeadline(newDeadlineRespondent1);
            caseData.setBusinessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM));
            caseData.setRespondent1(updatedRespondent1);
            caseData.setRespondent1Copy(null);
            caseData.setSolicitorReferencesCopy(null);
            caseData.setRespondent1ClaimResponseIntentionType(caseData.getRespondent1ClaimResponseIntentionType());
            caseData.setRespondent1ClaimResponseIntentionTypeApplicant2(
                caseData.getRespondent1ClaimResponseIntentionTypeApplicant2());
            caseData.setNextDeadline(newDeadlineRespondent1.toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(defaultCaseListReferences);
        } else if (caseData.getAddRespondent2() != null && caseData.getRespondent2() != null
            && respondent2HasSameLegalRep(caseData)) {
            //1v2 same
            var updatedRespondent2 = caseData.getRespondent2().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();

            caseData.setRespondent1AcknowledgeNotificationDate(time.now());
            caseData.setRespondent2AcknowledgeNotificationDate(time.now());
            caseData.setRespondent1ResponseDeadline(newDeadlineRespondent1);
            caseData.setRespondent2ResponseDeadline(newDeadlineRespondent1);
            caseData.setBusinessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM));
            caseData.setRespondent1(updatedRespondent1);
            caseData.setRespondent2(updatedRespondent2);
            caseData.setRespondent1Copy(null);
            caseData.setRespondent2Copy(null);
            caseData.setSolicitorReferencesCopy(null);
            caseData.setRespondent1ClaimResponseIntentionType(caseData.getRespondent1ClaimResponseIntentionType());
            caseData.setRespondent2ClaimResponseIntentionType(caseData.getRespondent2ClaimResponseIntentionType());
            caseData.setNextDeadline(newDeadlineRespondent1.toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(defaultCaseListReferences);
        } else if (caseData.getRespondent1() != null && caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2().equals(YES)
            && respondent1Check.equals(YES) && !respondent2HasSameLegalRep(caseData)) {
            //1v2 diff login 1

            caseData.setRespondent1AcknowledgeNotificationDate(time.now());
            caseData.setRespondent1(updatedRespondent1);
            caseData.setSolicitorReferences(solicitorReferencesCopy);
            caseData.setRespondent2(caseData.getRespondent2Copy());
            caseData.setRespondent1ClaimResponseIntentionType(caseData.getRespondent1ClaimResponseIntentionType());
            caseData.setBusinessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM));
            caseData.setRespondent1ResponseDeadline(newDeadlineRespondent1);
            caseData.setRespondent1Copy(null);
            caseData.setSolicitorReferencesCopy(null);
            caseData.setIsRespondent1(null);
            caseData.setCaseListDisplayDefendantSolicitorReferences(defaultCaseListReferences);
            caseData.setNextDeadline(deadlinesCalculator.nextDeadline(
                Arrays.asList(newDeadlineRespondent1, caseData.getRespondent2ResponseDeadline())).toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(
                solicitor1ReferenceFromCopy,
                respondentSolicitor2Reference));

        } else if (caseData.getAddRespondent2() != null && caseData.getAddRespondent2().equals(YES)
            && respondent1Check.equals(NO) && !respondent2HasSameLegalRep(caseData)) {
            var updatedRespondent2 = caseData.getRespondent2Copy().toBuilder()
                .primaryAddress(caseData.getRespondent2Copy().getPrimaryAddress())
                .build();
            //1v2 diff login 2
            caseData.setRespondent2AcknowledgeNotificationDate(time.now());
            caseData.setRespondent2(updatedRespondent2);
            caseData.setSolicitorReferences(solicitorReferencesCopy);
            caseData.setRespondent1Copy(null);
            caseData.setRespondent2Copy(null);
            caseData.setBusinessProcess(BusinessProcess.ready(ACKNOWLEDGE_CLAIM));
            caseData.setRespondent2ResponseDeadline(newDeadlineRespondent2);
            caseData.setRespondent2ClaimResponseIntentionType(caseData.getRespondent2ClaimResponseIntentionType());
            caseData.setIsRespondent1(null);
            caseData.setSolicitorReferencesCopy(null);
            caseData.setNextDeadline(deadlinesCalculator.nextDeadline(
                Arrays.asList(newDeadlineRespondent2, caseData.getRespondent1ResponseDeadline())).toLocalDate());
            caseData.setCaseListDisplayDefendantSolicitorReferences(getAllDefendantSolicitorReferences(
                solicitor1ReferenceFromCopy,
                respondentSolicitor2Reference));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
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
        return !solicitorRepresentsOnlyOneOrBothRespondents(callbackParams, RESPONDENTSOLICITORTWO);
    }

}
