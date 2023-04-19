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
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

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

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateValues,
            callbackKey(ABOUT_TO_SUBMIT), this::setBusinessProcess,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populateValues(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        var isApplicant = YesOrNo.NO;
        var isRespondent1 = YesOrNo.NO;
        var isRespondent2 = YesOrNo.NO;
        if (checkUserRoles(callbackParams, CaseRole.APPLICANTSOLICITORONE)) {
            isApplicant = YesOrNo.YES;
            updatedData.hearingDurationTextApplicant(formatHearingDuration(caseData.getHearingDuration()));
        } else if (checkUserRoles(callbackParams, CaseRole.RESPONDENTSOLICITORONE)) {
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
            .data(errors.size() == 0
                      ? updatedData.build().toMap(objectMapper) : null)
            .build();
    }

    private CallbackResponse setBusinessProcess(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();

        if (checkUserRoles(callbackParams, CaseRole.APPLICANTSOLICITORONE)) {
            if (caseData.getTrialReadyApplicant() == YesOrNo.YES) {
                updatedData.businessProcess(BusinessProcess.ready(APPLICANT_TRIAL_READY_NOTIFY_OTHERS));
            } else {
                updatedData.businessProcess(BusinessProcess.ready(GENERATE_TRIAL_READY_DOCUMENT_APPLICANT));
            }
        } else if (checkUserRoles(callbackParams, CaseRole.RESPONDENTSOLICITORONE)) {
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

        if (checkUserRoles(callbackParams, CaseRole.APPLICANTSOLICITORONE)) {
            return caseData.getTrialReadyApplicant();
        } else if (checkUserRoles(callbackParams, CaseRole.RESPONDENTSOLICITORONE)) {
            return caseData.getTrialReadyRespondent1();
        } else {
            return caseData.getTrialReadyRespondent2();
        }
    }

    private boolean checkUserRoles(CallbackParams callbackParams, CaseRole userRole) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        return coreCaseUserService.userHasCaseRole(callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid(), userRole);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
