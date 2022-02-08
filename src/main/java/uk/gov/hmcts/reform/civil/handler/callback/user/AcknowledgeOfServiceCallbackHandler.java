package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.*;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.*;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.READY;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcknowledgeOfServiceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ACKNOWLEDGEMENT_OF_SERVICE);

    public static final String CONFIRMATION_SUMMARY = "<br />You need to respond to the claim before %s."
        + "%n%n[Download the Acknowledgement of Service form](%s)";

    private final ExitSurveyContentService exitSurveyContentService;
    private final DateOfBirthValidator dateOfBirthValidator;
    private final DeadlinesCalculator deadlinesCalculator;
    private final ObjectMapper objectMapper;
    private final PostcodeValidator postcodeValidator;
    private final Time time;

    private final EventEmitterService eventEmitterService;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(V_1, ABOUT_TO_START), this::populateRespondent1Copy,
            callbackKey(MID, "confirm-details"), this::validateDateOfBirth,
            callbackKey(ABOUT_TO_SUBMIT), this::setNewResponseDeadline,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::setNewResponseDeadlineV1,
            callbackKey(SUBMITTED), this::buildConfirmation,
            callbackKey(MID, "specCorrespondenceAddress"), this::validateCorrespondenceApplicantAddress
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateRespondent1Copy(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        LocalDateTime dateTime = LocalDateTime.now();
        var updatedCaseData = caseData.toBuilder()
            .respondent1Copy(caseData.getRespondent1())
            .build();
        List<String> errors = new ArrayList<>();
        var responseDedline = caseData.getRespondent1ResponseDeadline();
        if (dateTime.toLocalDate().isAfter(responseDedline.toLocalDate())) {
            errors.add("Deadline to file Acknowledgement of Service has passed, option is not available.");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse validateCorrespondenceApplicantAddress(CallbackParams callbackParams) {
        if (callbackParams.getRequest().getEventId().equals("ACKNOWLEDGEMENT_OF_SERVICE")) {
            CaseData caseData = callbackParams.getCaseData();
            if (caseData.getSpecAoSApplicantCorrespondenceAddressRequired().equals(NO)) {
                List<String> errors = postcodeValidator.validatePostCodeForDefendant(
                    caseData.getSpecAoSApplicantCorrespondenceAddressdetails().getPostCode());

                return AboutToStartOrSubmitCallbackResponse.builder()
                    .errors(errors)
                    .build();
            } else {
                return AboutToStartOrSubmitCallbackResponse.builder()
                    .build();
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
        Party respondent = callbackParams.getCaseData().getRespondent1();
        List<String> errors = dateOfBirthValidator.validate(respondent);
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getSpecAoSRespondentCorrespondenceAddressRequired().equals(NO)) {
            errors = postcodeValidator.validatePostCodeForDefendant(
                caseData.getSpecAoSRespondentCorrespondenceAddressdetails().getPostCode());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    //@After("execution(* uk.gov.hmcts.reform.civil.aspect.EventEmitterAspect.emitBusinessProcessEvent(..))")
    private CallbackResponse setNewResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
        LocalDateTime newResponseDate = deadlinesCalculator.plus14DaysAt4pmDeadline(responseDeadline);

        CaseData caseDataUpdated = caseData.toBuilder()
            .respondent1AcknowledgeNotificationDate(time.now())
            .respondent1ResponseDeadline(newResponseDate)
            .businessProcess(BusinessProcess.ready(ACKNOWLEDGEMENT_OF_SERVICE))
            .specRespondentCorrespondenceAddressRequired(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())
            .specRespondentCorrespondenceAddressdetails(caseData.getSpecAoSApplicantCorrespondenceAddressdetails())
            .respondentSolicitor1ServiceAddressRequired(caseData.getSpecAoSRespondentCorrespondenceAddressRequired())
            .respondentSolicitor1ServiceAddress(caseData.getSpecAoSRespondentCorrespondenceAddressdetails())
            .build();

        //TODO - These change would need to be moved to CCUI - /case/trigger/events calls to CCD
        log.info(time.now() + "Before saving data to CCD");
        StartEventResponse startEventResponse = coreCaseDataService.startUpdate("1643989832161728", ACKNOWLEDGEMENT_OF_SERVICE);
        BusinessProcess businessProcess = caseDataUpdated.getBusinessProcess();
        CaseData data = caseDetailsConverter.toCaseData(startEventResponse.getCaseDetails());
        coreCaseDataService.submitUpdate("1643989832161728", caseDataContent(startEventResponse, businessProcess));
        log.info(time.now() + ": After saving data to CCD");
        //TODO - These change would need to be moved to CCUI - /case/trigger/events calls to CCD

        //TODO: call EventEmitterAspect by mocking callBackParm as submitted
        if (caseDataUpdated.getBusinessProcess() != null && caseDataUpdated.getBusinessProcess().getStatus() == READY) {
            eventEmitterService.emitBusinessProcessCamundaEvent(caseDataUpdated, false);
            log.info("Event emitted successfully");
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

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
            .businessProcess(BusinessProcess.ready(ACKNOWLEDGEMENT_OF_SERVICE))
            .respondent1(updatedRespondent1)
            .respondent1Copy(null)
            .specRespondentCorrespondenceAddressRequired(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())
            .specRespondentCorrespondenceAddressdetails(caseData.getSpecAoSApplicantCorrespondenceAddressdetails())
            .respondentSolicitor1ServiceAddressRequired(caseData.getSpecAoSRespondentCorrespondenceAddressRequired())
            .respondentSolicitor1ServiceAddress(caseData.getSpecAoSRespondentCorrespondenceAddressdetails())
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    private CaseDataContent caseDataContent(StartEventResponse startEventResponse, BusinessProcess businessProcess) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.put("businessProcess", businessProcess);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData caseDataUpdated = caseData.toBuilder()
            .respondent1AcknowledgeNotificationDate(time.now())
            .businessProcess(BusinessProcess.ready(ACKNOWLEDGEMENT_OF_SERVICE))
            .build();

        String body = format(
            CONFIRMATION_SUMMARY,
            formatLocalDateTime(caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT),
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference())
        )
            + exitSurveyContentService.respondentSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# You have acknowledged the claim%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
            .build();
    }
}
