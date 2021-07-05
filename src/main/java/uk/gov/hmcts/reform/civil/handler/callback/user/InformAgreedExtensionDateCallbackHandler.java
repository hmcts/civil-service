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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.civil.validation.DeadlineExtensionValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INFORM_AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_BUSINESS_DAY;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
public class InformAgreedExtensionDateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(INFORM_AGREED_EXTENSION_DATE);

    private final ExitSurveyContentService exitSurveyContentService;
    private final DeadlineExtensionValidator validator;
    private final ObjectMapper objectMapper;
    private final DeadlinesCalculator deadlinesCalculator;
    private final Time time;
    private final CoreCaseUserService coreCaseUserService;
    private final StateFlowEngine stateFlowEngine;

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
        return EVENTS;
    }

    private CallbackResponse populateIsRespondent1Flag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        var isRespondent1 = YES;
        if (stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && hasRespondentSolicitorTwoRole(callbackParams)) {
            isRespondent1 = NO;
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().isRespondent1(isRespondent1).build().toMap(objectMapper))
            .build();
    }

    private boolean hasRespondentSolicitorTwoRole(CallbackParams callbackParams) {
        return coreCaseUserService.userHasCaseRole(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            RESPONDENTSOLICITORTWO
        );
    }

    private CallbackResponse validateExtensionDate(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
        if (caseData.getIsRespondent1() == NO) {
            agreedExtension = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
        }
        //TODO: update to get correct deadline as a part of CMC-1346
        LocalDateTime currentResponseDeadline = caseData.getRespondent1ResponseDeadline();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validator.validateProposedDeadline(agreedExtension, currentResponseDeadline))
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
            .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse setResponseDeadlineV1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate agreedExtension = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
        if (caseData.getIsRespondent1() == NO) {
            agreedExtension = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
        }
        LocalDateTime newDeadline = deadlinesCalculator.calculateFirstWorkingDay(agreedExtension)
            .atTime(END_OF_BUSINESS_DAY);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(INFORM_AGREED_EXTENSION_DATE))
            .isRespondent1(null);

        if (caseData.getIsRespondent1() == YES) {
            caseDataBuilder.respondent1TimeExtensionDate(time.now())
                .respondent1ResponseDeadline(newDeadline);
        } else {
            caseDataBuilder.respondent2TimeExtensionDate(time.now())
                .respondent2ResponseDeadline(newDeadline);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();

        String body = format(
            "<br />You must respond to the claimant by %s",
            formatLocalDateTime(responseDeadline, DATE_TIME_AT)) + exitSurveyContentService.respondentSurvey();
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Extension deadline submitted")
            .confirmationBody(body)
            .build();
    }
}
