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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.utils.UserRoleCaching;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT2;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.TRIAL_READINESS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLICANT_TRIAL_READY_NOTIFY_OTHERS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.formatHearingDuration;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;

@Service
@RequiredArgsConstructor
public class TrialReadinessCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(TRIAL_READINESS);
    private static final String TOO_LATE = "Trial arrangements had to be confirmed more than 3 weeks before the trial.";
    private static final String NO_SMALL_CLAIMS = "This event is not available for small claims cases.";
    public static final String READY_HEADER = "## You have said this case is ready for trial or hearing";
    public static final String READY_BODY = "### What happens next \n\n"
        + "You can view your and other party's trial arrangements in documents in the case details.\n\n "
        + "If there are any additional changes between now and the hearing date, "
        + "you will need to make an application as soon as possible and pay the appropriate fee.";
    public static final String NOT_READY_HEADER = "## You have said this case is not ready for trial or hearing";
    public static final String NOT_READY_BODY = "### What happens next \n\n"
        + "You can view your and other party's trial arrangements in documents in the case details. "
        + "If there are any additional changes between now and the hearing date, "
        + "you will need to make an application as soon as possible and pay the appropriate fee.\n\n"
        + "The trial will go ahead on the specified date "
        + "unless a judge makes an order changing the date of the hearing. "
        + "If you want the date of the hearing to be changed (or any other order to make the case ready for trial)"
        + "you will need to make an application to the court and pay the appropriate fee.";
    private final ObjectMapper objectMapper;

    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    private final UserRoleCaching userRoleCaching;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateValues,
            callbackKey(ABOUT_TO_SUBMIT), this::setBusinessProcess,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    CallbackResponse populateValues(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        String bearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String ccdCaseRef = callbackParams.getCaseData().getCcdCaseReference().toString();
        String keyToken = userRoleCaching.getCacheKeyToken(bearerToken);
        List<String> userRoles = userRoleCaching.getUserRoles(bearerToken, ccdCaseRef, keyToken);

        var isApplicant = YesOrNo.NO;
        var isRespondent1 = YesOrNo.NO;
        var isRespondent2 = YesOrNo.NO;
        if (isApplicantSolicitor(userRoles)) {
            isApplicant = YesOrNo.YES;
            updatedData.hearingDurationTextApplicant(formatHearingDuration(caseData.getHearingDuration()));
        } else if (isRespondentSolicitorOne(userRoles)) {
            isRespondent1 = YesOrNo.YES;
            updatedData.hearingDurationTextRespondent1(formatHearingDuration(caseData.getHearingDuration()));
        } else {
            isRespondent2 = YesOrNo.YES;
            updatedData.hearingDurationTextRespondent2(formatHearingDuration(caseData.getHearingDuration()));
        }

        updatedData.isApplicant1(isApplicant);
        updatedData.isRespondent1(isRespondent1);
        updatedData.isRespondent2(isRespondent2);

        ArrayList<String> errors = new ArrayList<>();
        if (nonNull(caseData.getHearingDate())
            && caseData.getHearingDate().minusWeeks(3).isBefore(LocalDate.now())) {
            errors.add(format(TOO_LATE));
        }

        if (SMALL_CLAIM.equals(caseData.getAllocatedTrack())) {
            errors.add(format(NO_SMALL_CLAIMS));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(errors.isEmpty() ? updatedData.build().toMap(objectMapper) : null)
            .build();
    }

    CallbackResponse setBusinessProcess(CallbackParams callbackParams) {

        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        String bearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String ccdCaseRef = callbackParams.getCaseData().getCcdCaseReference().toString();
        String keyToken = userRoleCaching.getCacheKeyToken(bearerToken);
        List<String> roles = userRoleCaching.getUserRoles(bearerToken, ccdCaseRef, keyToken);

        if (isApplicantSolicitor(roles) || isLIPClaimant(roles)) {
            if (caseData.getTrialReadyApplicant() == YesOrNo.YES) {
                updatedData.businessProcess(BusinessProcess.ready(APPLICANT_TRIAL_READY_NOTIFY_OTHERS));
            } else {
                updatedData.businessProcess(BusinessProcess.ready(GENERATE_TRIAL_READY_DOCUMENT_APPLICANT));
            }
        } else if (isRespondentSolicitorOne(roles) || isLIPDefendant(roles)) {
            if (caseData.getTrialReadyRespondent1() == YesOrNo.YES) {
                updatedData.businessProcess(BusinessProcess.ready(RESPONDENT1_TRIAL_READY_NOTIFY_OTHERS));
            } else {
                updatedData.businessProcess(BusinessProcess.ready(GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT1));
            }
        } else {
            if (caseData.getTrialReadyRespondent2() == YesOrNo.YES) {
                updatedData.businessProcess(BusinessProcess.ready(RESPONDENT2_TRIAL_READY_NOTIFY_OTHERS));
            } else {
                updatedData.businessProcess(BusinessProcess.ready(GENERATE_TRIAL_READY_DOCUMENT_RESPONDENT2));
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(checkUserReady(callbackParams).equals(YesOrNo.YES) ? READY_HEADER : NOT_READY_HEADER)
            .confirmationBody(checkUserReady(callbackParams).equals(YesOrNo.YES) ? READY_BODY : NOT_READY_BODY)
            .build();
    }

    private YesOrNo checkUserReady(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        String bearerToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String ccdCaseRef = callbackParams.getCaseData().getCcdCaseReference().toString();
        String keyToken = userRoleCaching.getCacheKeyToken(bearerToken);
        List<String> userRoles = userRoleCaching.getUserRoles(bearerToken, ccdCaseRef, keyToken);

        if (isApplicantSolicitor(userRoles) || isLIPClaimant(userRoles)) {
            return caseData.getTrialReadyApplicant();
        } else if (isRespondentSolicitorOne(userRoles) || isLIPDefendant(userRoles)) {
            return caseData.getTrialReadyRespondent1();
        } else {
            return caseData.getTrialReadyRespondent2();
        }
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
