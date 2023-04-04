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
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class ResponseToDefenceSpecV2 extends ResponseToDefenceSpecV1 {

    public ResponseToDefenceSpecV2(FeatureToggleService featureToggleService, LocationRefDataService locationRefDataService, CourtLocationUtils courtLocationUtils) {
        super(featureToggleService, locationRefDataService, courtLocationUtils);
    }

    @Override
    public CallbackResponse populateCaseData(CallbackParams callbackParams, ObjectMapper objectMapper) {
        CaseData caseData = updateCaseDataWithRespondent1Copy(callbackParams);
        var caseDataBuilder = caseData.builder();
        populateDQCourtLocations(caseDataBuilder, callbackParams);
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
