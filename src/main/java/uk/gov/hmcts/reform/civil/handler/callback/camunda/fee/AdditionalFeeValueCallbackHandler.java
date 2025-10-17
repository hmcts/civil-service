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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.GaCallbackDataUtil;
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
        GeneralApplicationCaseData gaCaseData = GaCallbackDataUtil.resolveGaCaseData(callbackParams, objectMapper);
        CaseData caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, callbackParams.getCaseData(), objectMapper);

        if (caseData == null) {
            throw new IllegalArgumentException("Case data missing from callback params");
        }

        if (judicialDecisionHelper.isApplicationUncloakedWithAdditionalFee(caseData)) {
            Fee feeForGA = feeService.getFeeForGA(feesConfiguration.getApplicationUncloakAdditionalFee(), null, null);
            GAPbaDetails existingDetails = Optional.ofNullable(gaCaseData)
                .map(GeneralApplicationCaseData::getGeneralAppPBADetails)
                .orElse(caseData.getGeneralAppPBADetails());
            BigDecimal applicationFee = Optional.ofNullable(existingDetails)
                .map(GAPbaDetails::getFee)
                .map(Fee::getCalculatedAmountInPence)
                .orElse(null);
            GAPbaDetails generalAppPBADetails = Optional.ofNullable(existingDetails)
                .map(GAPbaDetails::toBuilder)
                .orElse(GAPbaDetails.builder())
                .fee(feeForGA)
                .build();
            CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
                .generalAppPBADetails(generalAppPBADetails);
            YesOrNo isGaApplicantLip = caseData.getIsGaApplicantLip();
            if (isGaApplicantLip == null && gaCaseData != null) {
                isGaApplicantLip = gaCaseData.getIsGaApplicantLip();
            }
            if (featureToggleService.isGaForLipsEnabled() && isGaApplicantLip == YesOrNo.YES) {
                builder.applicationFeeAmountInPence(applicationFee);
            }
            caseData = builder.build();
            if (gaCaseData != null) {
                gaCaseData = gaCaseData.toBuilder()
                    .generalAppPBADetails(generalAppPBADetails)
                    .build();
                caseData = GaCallbackDataUtil.mergeToCaseData(gaCaseData, caseData, objectMapper);
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
