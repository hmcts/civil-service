package uk.gov.hmcts.reform.civil.handler.callback.camunda.fee;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;
import uk.gov.hmcts.reform.civil.service.JudicialDecisionHelper;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.OBTAIN_ADDITIONAL_FEE_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdditionalFeeValueCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(OBTAIN_ADDITIONAL_FEE_VALUE);
    private static final String TASK_ID = "ObtainAdditionalFeeValue";
    private final GeneralAppFeesService feeService;
    private final GeneralAppFeesConfiguration feesConfiguration;
    private final JudicialDecisionHelper judicialDecisionHelper;
    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    public String camundaActivityId() {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::getAdditionalFeeValue
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse getAdditionalFeeValue(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        if (judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)) {
            Fee feeForGA = feeService.getFeeForGA(feesConfiguration.getApplicationUncloakAdditionalFee(), null, null);
            BigDecimal applicationFee = Optional.ofNullable(caseData.getGeneralAppPBADetails())
                .map(GAPbaDetails::getFee)
                .map(Fee::getCalculatedAmountInPence)
                .orElse(null);
            GAPbaDetails generalAppPBADetails = caseData.getGeneralAppPBADetails()
                .toBuilder().fee(feeForGA).build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder();
            builder.generalAppPBADetails(generalAppPBADetails);
            if (featureToggleService.isGaForLipsEnabled() && caseData.getIsGaApplicantLip() == YesOrNo.YES) {
                builder.applicationFeeAmountInPence(applicationFee);
            }
            caseData = builder.build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
