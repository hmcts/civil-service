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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.NotSuitableSdoOptions;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.OtherDetails;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TransferCaseDetails;
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
    public static final String NOT_SUITABLE_SDO_CONFIRMATION_BODY = "<br />If a Judge has submitted this information, "
        + "a notification will be sent to the listing officer to look at this case offline."
        + "%n%nIf a legal adviser has submitted this information a notification will be sent to a judge for review.";

    public static final String NOT_SUITABLE_SDO_TRANSFER_CASE_CONFIRMATION_BODY = "<br />A notification will be sent" +
        " to the listing officer to look at this case and process the transfer of case.";

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
        OtherDetails tempOtherDetails = OtherDetails.builder()
            .notSuitableForSDO(YesOrNo.YES)
            .build();
        if (toggleService.isTransferOnlineCaseEnabled()) {
            if (callbackParams.getCaseData().getNotSuitableSdoOptions() == NotSuitableSdoOptions.CHANGE_LOCATION) {
                dataBuilder.notSuitableSdoOptions(NotSuitableSdoOptions.CHANGE_LOCATION);
                TransferCaseDetails transferCaseDetails = TransferCaseDetails.builder()
                    .reasonForTransferCaseTxt(callbackParams.getCaseData().getTocTransferCaseReason().getReasonForCaseTransferJudgeTxt())
                    .build();
                dataBuilder.transferCaseDetails(transferCaseDetails).build();
            } else {
                dataBuilder.notSuitableSdoOptions(NotSuitableSdoOptions.OTHER_REASONS);
                tempOtherDetails.setReasonNotSuitableForSDO(callbackParams.getCaseData().getReasonNotSuitableSDO().getInput());
            }
        } else {
            tempOtherDetails.setReasonNotSuitableForSDO(callbackParams.getCaseData().getReasonNotSuitableSDO().getInput());
        }
        dataBuilder.otherDetails(tempOtherDetails).build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateNotSuitableReason(CallbackParams callbackParams) {
        final int lengthAllowed = 150;
        List<String> errors = new ArrayList<>();
        String reason;

        if (toggleService.isSdoR2Enabled()
            && callbackParams.getCaseData().getNotSuitableSdoOptions() == NotSuitableSdoOptions.CHANGE_LOCATION
            && callbackParams.getCaseData().getCcdState() == CaseState.CASE_PROGRESSION) {
            errors.add(
                "Unable to process this request. To transfer the case to another court you need to issue a General Order.");
        }

        if (isTransferOnlineCase(callbackParams.getCaseData())) {
            reason = callbackParams.getCaseData().getTocTransferCaseReason().getReasonForCaseTransferJudgeTxt();
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
        if (isTransferOnlineCase(caseData)) {
            return dataBuilder;
        } else {
            dataBuilder.businessProcess(BusinessProcess.ready(NotSuitable_SDO));
        }
        return dataBuilder;
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isTransferOnlineCase(caseData)) {
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
        return format(NOT_SUITABLE_SDO_CONFIRMATION_BODY);
    }

    private boolean isTransferOnlineCase(CaseData caseData) {
        return toggleService.isTransferOnlineCaseEnabled() && caseData.getNotSuitableSdoOptions()
            == NotSuitableSdoOptions.CHANGE_LOCATION;
    }

    private String getHeaderTOC(CaseData caseData) {
        return format("# Your request was successful%n## This claim will be transferred to a different location");
    }

    private String getBodyTOC(CaseData caseData) {
        return format(NOT_SUITABLE_SDO_TRANSFER_CASE_CONFIRMATION_BODY);
    }
}
