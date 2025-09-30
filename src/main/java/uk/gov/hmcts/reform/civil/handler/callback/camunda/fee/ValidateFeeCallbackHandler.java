package uk.gov.hmcts.reform.civil.handler.callback.camunda.fee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GaForLipService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_FEE_GASPEC;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidateFeeCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(VALIDATE_FEE_GASPEC);
    private static final String ERROR_MESSAGE_NO_FEE_IN_CASEDATA = "Application case data does not have fee details";
    private static final String ERROR_MESSAGE_FEE_CHANGED = "Fee has changed since application was submitted. "
        + "It needs to be validated again";
    private static final String TASK_ID = "GeneralApplicationValidateFee";

    private final GeneralAppFeesService feeService;
    private final FeatureToggleService featureToggleService;
    private final GaForLipService gaForLipService;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::validateFee
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateFee(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (!featureToggleService.isGaForLipsEnabled() && !gaForLipService.isGaForLip(caseData)) {
            Fee feeForGA = feeService.getFeeForGA(caseData);
            errors = compareFees(caseData, feeForGA);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private List<String> compareFees(CaseData caseData, Fee latestfee) {

        if (caseData.getGeneralAppPBADetails() == null
            || caseData.getGeneralAppPBADetails().getFee() == null) {
            return List.of(ERROR_MESSAGE_NO_FEE_IN_CASEDATA);
        }
        Fee caseDataFee = caseData.getGeneralAppPBADetails().getFee();
        if (!caseDataFee.equals(latestfee)) {
            log.info("Fees not equal - latest fee {} for General Application with value: {} with casedata fee {} with value : {} ",
                latestfee.getCode(), latestfee.getCalculatedAmountInPence(),  caseData.getGeneralAppPBADetails().getFee().getCode(),
                caseData.getGeneralAppPBADetails().getFee().getCalculatedAmountInPence());
            return List.of(ERROR_MESSAGE_FEE_CHANGED);
        }

        return new ArrayList<>();
    }

}
