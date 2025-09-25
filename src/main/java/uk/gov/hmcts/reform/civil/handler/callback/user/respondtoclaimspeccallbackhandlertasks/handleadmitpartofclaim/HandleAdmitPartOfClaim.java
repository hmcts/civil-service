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
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleAdmitPartOfClaim implements CaseTask {

    public static final String ERROR_RESPONSE_TO_CLAIM_OWING_AMOUNT = "This amount equals or exceeds the claim amount plus interest.";
    private final ObjectMapper objectMapper;
    private final PaymentDateValidator paymentDateValidator;
    private final FeatureToggleService featureToggleService;
    private final List<HandleAdmitPartOfClaimCaseUpdater> handleAdmitPartOfClaimCaseUpdaters;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Executing HandleAdmitPartOfClaim for caseId: {}", caseData.getCcdCaseReference());

        List<String> errors = validatePaymentDate(caseData);

        if (featureToggleService.isLrAdmissionBulkEnabled()) {
            validateAdmittedClaimOwingAmount(errors, caseData);
        }

        if (!errors.isEmpty()) {
            return buildErrorResponse(errors);
        }

        if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec())
                && featureToggleService.isDefendantNoCOnlineForCase(caseData) && YES.equals(caseData.getIsRespondent1())
                && caseData.getSpecDefenceFullAdmittedRequired() == null) {
            caseData = caseData.toBuilder().specDefenceFullAdmittedRequired(NO).build();
        }

        if (RespondentResponseTypeSpec.FULL_ADMISSION.equals(caseData.getRespondent2ClaimResponseTypeForSpec())
                && featureToggleService.isDefendantNoCOnlineForCase(caseData) && YES.equals(caseData.getIsRespondent2())
                && caseData.getSpecDefenceFullAdmitted2Required() == null) {
            caseData = caseData.toBuilder().specDefenceFullAdmitted2Required(NO).build();
        }

        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        CaseData finalCaseData = caseData;
        handleAdmitPartOfClaimCaseUpdaters.forEach(updater -> updater.update(finalCaseData, updatedCaseData));
        updateResponseClaimTrack(callbackParams, caseData, updatedCaseData);

        return buildSuccessResponse(updatedCaseData);
    }

    private List<String> validatePaymentDate(CaseData caseData) {
        log.info("Validating payment date for caseId: {}", caseData.getCcdCaseReference());
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToAdmittedClaim())
                .orElseGet(() -> RespondToClaim.builder().build()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    private void updateResponseClaimTrack(CallbackParams callbackParams, CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Updating response claim track for caseId: {}", caseData.getCcdCaseReference());
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCaseData.responseClaimTrack(allocatedTrack.name());
        }
    }

    private CallbackResponse buildSuccessResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Successfully updated case data for HandleAdmitPartOfClaim");
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedCaseData.build().toMap(objectMapper))
                .build();
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        log.info("Determining allocated track for caseId: {}", caseData.getCcdCaseReference());
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null,
                featureToggleService, caseData);
    }

    private void validateAdmittedClaimOwingAmount(List<String> errors, CaseData caseData) {
        log.info("Validating admitted claim owing amount for caseId: {}", caseData.getCcdCaseReference());
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                && caseData.getSpecDefenceAdmittedRequired() == NO
                && YES.equals(caseData.getIsRespondent1())
                && nonNull(caseData.getRespondToAdmittedClaimOwingAmount())) {
            validateClaimOwingAmount(
                    errors,
                    caseData.getRespondToAdmittedClaimOwingAmount(),
                    caseData.getTotalClaimAmountPlusInterestAdmitPart()
            );
        }
        if (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                && caseData.getSpecDefenceAdmitted2Required() == NO
                && YES.equals(caseData.getIsRespondent2())
                && nonNull(caseData.getRespondToAdmittedClaimOwingAmount2())) {
            validateClaimOwingAmount(
                    errors,
                    caseData.getRespondToAdmittedClaimOwingAmount2(),
                    caseData.getTotalClaimAmountPlusInterestAdmitPart()
            );
        }
    }

    private void validateClaimOwingAmount(List<String> errors, BigDecimal admittedClaimOwingAmount, BigDecimal claimAmountWithInterest) {
        BigDecimal claimAmountWithInterestMinusPence = claimAmountWithInterest.subtract(BigDecimal.valueOf(0.01));
        if (MonetaryConversions.penniesToPounds(admittedClaimOwingAmount).compareTo(claimAmountWithInterestMinusPence) > 0) {
            errors.add(ERROR_RESPONSE_TO_CLAIM_OWING_AMOUNT);
        }
    }
}
