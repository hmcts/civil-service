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
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
public class InformAgreedExtensionDateForSpecCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(INFORM_AGREED_EXTENSION_DATE_SPEC);

    private final ExitSurveyContentService exitSurveyContentService;
    private final DeadlineExtensionValidator validator;
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;
    private final CoreCaseUserService coreCaseUserService;
    private final StateFlowEngine stateFlowEngine;
    private final UserService userService;
    private final FeatureToggleService toggleService;
    public static final String SPEC_ACKNOWLEDGEMENT_OF_SERVICE = "ACKNOWLEDGEMENT_OF_SERVICE";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(V_1, ABOUT_TO_START), this::populateIsRespondent1Flag,
            callbackKey(MID, "extension-date"), this::validateExtensionDate,
            callbackKey(ABOUT_TO_SUBMIT), this::setResponseDeadline,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::setResponseDeadlineV1,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        if (toggleService.isLrSpecEnabled()) {
            return EVENTS;
        } else {
            return Collections.emptyList();
        }
    }

    private CallbackResponse populateIsRespondent1Flag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var isRespondent1 = YES;
        if (solicitorRepresentsOnlyRespondent2(callbackParams)) {
            isRespondent1 = NO;
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().isRespondent1(isRespondent1).build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateExtensionDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = caseData.getRespondentSolicitor1AgreedDeadlineExtension();

        LocalDateTime currentResponseDeadline = caseData.getRespondent1ResponseDeadline();
        var isAoSApplied = caseData.getBusinessProcess().getCamundaEvent().equals(SPEC_ACKNOWLEDGEMENT_OF_SERVICE);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validator.specValidateProposedDeadline(agreedExtension, currentResponseDeadline, isAoSApplied))
            .build();
    }

    private CallbackResponse setResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
        LocalDateTime newDeadline = deadlinesCalculator.calculateFirstWorkingDay(agreedExtension)
            .atTime(END_OF_BUSINESS_DAY);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder()
            .respondent1TimeExtensionDate(time.now())
            .respondent1ResponseDeadline(newDeadline)
            .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE_SPEC));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse setResponseDeadlineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = solicitorRepresentsOnlyRespondent2(callbackParams)
            ? caseData.getRespondentSolicitor2AgreedDeadlineExtension()
            : caseData.getRespondentSolicitor1AgreedDeadlineExtension();
        LocalDateTime newDeadline = deadlinesCalculator.calculateFirstWorkingDay(agreedExtension)
            .atTime(END_OF_BUSINESS_DAY);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder().isRespondent1(null);

        if (caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES) {

            caseDataBuilder
                .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE_SPEC))
                .respondent1TimeExtensionDate(time.now())
                .respondent1ResponseDeadline(newDeadline)
                .respondent2TimeExtensionDate(time.now())
                .respondent2ResponseDeadline(newDeadline);
        } else if (solicitorRepresentsOnlyRespondent2(callbackParams)) {
            caseDataBuilder
                .respondent2TimeExtensionDate(time.now())
                .respondent2ResponseDeadline(newDeadline);
        } else {
            caseDataBuilder.respondent1TimeExtensionDate(time.now())
                .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE_SPEC))
                .respondent1ResponseDeadline(newDeadline);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String body;
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();

        body = format(
            "<h2 class=\"govuk-heading-m\">What happens next</h2>You need to respond before %s",
            formatLocalDateTime(responseDeadline, DATE_TIME_AT)
        ) + exitSurveyContentService.respondentSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Extension deadline submitted")
            .confirmationBody(body)
            .build();
    }

    private boolean solicitorRepresentsOnlyRespondent2(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO
        );
    }
}
