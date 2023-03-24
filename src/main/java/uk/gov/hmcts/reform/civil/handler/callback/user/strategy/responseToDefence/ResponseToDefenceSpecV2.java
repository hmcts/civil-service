package uk.gov.hmcts.reform.civil.handler.callback.user.strategy.responseToDefence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResponseToDefenceSpecV2 extends ResponseToDefenceSpecStrategy {

    private final FeatureToggleService featureToggleService;
    @Override
    public CallbackResponse populateCaseData(CallbackParams callbackParams, ObjectMapper objectMapper) {
        CaseData caseData = updateCaseDataWithRespondent1Copy(callbackParams);
        var caseDataBuilder = caseData.builder();
        if(featureToggleService.isPinInPostEnabled()) {
            ResponseOneVOneShowTag responseOneVOneShowTag = caseData.getResponseOneVOneShowTag();
            caseDataBuilder.showResponseOneVOneFlag(responseOneVOneShowTag);
            caseDataBuilder.respondent1PaymentDateToStringSpec(caseData.getPayDateAsString());

            Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .map(RespondToClaim::getHowMuchWasPaid);

            howMuchWasPaid.ifPresent(howMuchWasPaidValue -> caseDataBuilder.partAdmitPaidValuePounds(
                MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
