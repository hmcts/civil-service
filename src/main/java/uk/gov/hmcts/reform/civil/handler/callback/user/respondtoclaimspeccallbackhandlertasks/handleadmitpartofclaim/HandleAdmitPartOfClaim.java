package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.handleadmitpartofclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleAdmitPartOfClaim implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;
    private final List<HandleAdmitPartOfClaimCaseUpdater> handleAdmitPartOfClaimCaseUpdaters;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validatePaymentDate(caseData);
        if (!errors.isEmpty()) {
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        handleAdmitPartOfClaimCaseUpdaters.forEach(updater -> updater.update(caseData, updatedCaseData));
        updateResponseClaimTrack(callbackParams, caseData, updatedCaseData);

        return buildSuccessResponse(updatedCaseData);
    }

    private List<String> validatePaymentDate(CaseData caseData) {
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .orElseGet(() -> RespondToClaim.builder().build()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    private void updateResponseClaimTrack(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCaseData.responseClaimTrack(allocatedTrack.name());
        }
    }

    private CallbackResponse buildSuccessResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.build().toMap(objectMapper))
                .build();
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null,
                toggleService, caseData);
    }
}
