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
import uk.gov.hmcts.reform.civil.model.transferonlinecase.NotSuitableSdoOptions;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.OtherDetails;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TocTransferCaseReason;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NotSuitable_SDO;

@Service
@RequiredArgsConstructor
public class NotSuitableSDOCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(NotSuitable_SDO);
    public static final String NotSuitableSDO_CONFIRMATION_BODY = "<br />If a Judge has submitted this information, "
        + "a notification will be sent to the listing officer to look at this case offline."
        + "%n%nIf a legal adviser has submitted this information a notification will be sent to a judge for review.";

    private final ObjectMapper objectMapper;

    private final Time time;

    private final FeatureToggleService toggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::addUnsuitableSDODate)
            .put(callbackKey(MID, "not-suitable-reason"), this::validateNotSuitableReason)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitNotSuitableSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse submitNotSuitableSDO(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder dataBuilder = getSharedData(callbackParams);

        if (toggleService.isTransferOnlineCaseEnabled()
            && (callbackParams.getCaseData().getNotSuitableSdoOptions() == NotSuitableSdoOptions.CHANGE_LOCATION)) {
                TocTransferCaseReason tocTransferCaseReason = TocTransferCaseReason.builder()
                    .reasonForCaseTransferJudgeTxt(callbackParams.getCaseData().getTocTransferCaseReason().getReasonForCaseTransferJudgeTxt())
                    .build();
                dataBuilder.tocTransferCaseReason(tocTransferCaseReason).build();
        }
        OtherDetails tempOtherDetails = OtherDetails.builder()
            .notSuitableForSDO(YesOrNo.YES)
            .reasonNotSuitableForSDO(callbackParams.getCaseData().getReasonNotSuitableSDO().getInput())
            .build();
        dataBuilder.otherDetails(tempOtherDetails).build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateNotSuitableReason(CallbackParams callbackParams) {
        final int lengthAllowed = 150;
        List<String> errors = new ArrayList<>();
        String reason;
        if (toggleService.isTransferOnlineCaseEnabled()
            && (callbackParams.getCaseData().getNotSuitableSdoOptions() == NotSuitableSdoOptions.CHANGE_LOCATION)) {
                reason = ""; //Change to ReasonForCaseTransferJudgeTxt if validation also needed for this field
        } else {
            reason = callbackParams.getCaseData().getReasonNotSuitableSDO().getInput();
        }
        if (reason.length() > lengthAllowed) {
            errors.add("Character Limit Reached: "
                + "Reason for not drawing Standard Directions order cannot exceed "
                + lengthAllowed + " characters.");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse addUnsuitableSDODate(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder dataBuilder = callbackParams.getCaseData().toBuilder();

        dataBuilder.unsuitableSDODate(time.now());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CaseData.CaseDataBuilder getSharedData(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();
        if (toggleService.isTransferOnlineCaseEnabled()
            && (callbackParams.getCaseData().getNotSuitableSdoOptions() == NotSuitableSdoOptions.CHANGE_LOCATION)) {
                //TODO add new event
        } else {
            dataBuilder.businessProcess(BusinessProcess.ready(NotSuitable_SDO));
        }
        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (toggleService.isTransferOnlineCaseEnabled()
            && (callbackParams.getCaseData().getNotSuitableSdoOptions() == NotSuitableSdoOptions.CHANGE_LOCATION)) {
                return SubmittedCallbackResponse.builder()
                    .confirmationHeader(getHeaderTOC(caseData))
                    .confirmationBody(getBodyTOC(caseData))
                    .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getHeader(caseData))
                .confirmationBody(getBody(caseData))
                .build();
        }
    }

    private String getHeader(CaseData caseData) {
        return format("# Your request was accepted%n## Case has now moved offline");
    }

    private String getBody(CaseData caseData) {
        return format(NotSuitableSDO_CONFIRMATION_BODY);
    }

    private String getHeaderTOC(CaseData caseData) {
        //TODO change for transfer online case confirmation message
        return format("# Your request was accepted%n## Case has now moved...");
    }

    private String getBodyTOC(CaseData caseData) {
        //TODO change for transfer online case confirmation message
        return format("TOC body");
    }
}
