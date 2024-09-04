package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Component
@RequiredArgsConstructor
public class HandleDefendAllClaim implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;
    private final RespondToClaimSpecDocumentHandler RespondToClaimSpecDocumentHandler;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(
            Optional.ofNullable(caseData.getRespondToClaim()).orElseGet(RespondToClaim::new)
        );

        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
        }

        CaseData.CaseDataBuilder<?, ?> updatedCase = caseData.toBuilder();
        updatedCase.showConditionFlags(whoDisputesFullDefence(caseData));

        if (DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            handleDefendantResponseSpec(caseData, updatedCase);
        }

        return AboutToStartOrSubmitCallbackResponse.builder().data(updatedCase.build().toMap(objectMapper)).build();
    }

    private void handleDefendantResponseSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase) {
        RespondToClaimSpecDocumentHandler.populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase);

        if (shouldMarkAsSpecPaidLessOrDisputesOrPartAdmission(caseData)) {
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            if (shouldMarkAsSpecDisputesOrPartAdmissionForRespondent2(caseData)) {
                updatedCase.specDisputesOrPartAdmission(YES);
            } else {
                updatedCase.specDisputesOrPartAdmission(NO);
            }
        } else {
            if (shouldMarkAsSpecDisputesOrPartAdmissionForRespondent1(caseData)) {
                updatedCase.specDisputesOrPartAdmission(YES);
            } else {
                updatedCase.specDisputesOrPartAdmission(NO);
            }
        }

        AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
        updatedCase.responseClaimTrack(allocatedTrack.name());
    }

    public Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        return RespondToClaimSpecUtilsDisputeDetails.whoDisputesFullDefence(caseData);
    }

    private boolean shouldMarkAsSpecPaidLessOrDisputesOrPartAdmission(CaseData caseData) {
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == PART_ADMISSION;
    }

    private boolean shouldMarkAsSpecDisputesOrPartAdmissionForRespondent2(CaseData caseData) {
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() != RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
            || caseData.getRespondent2ClaimResponseTypeForSpec() == PART_ADMISSION);
    }

    private boolean shouldMarkAsSpecDisputesOrPartAdmissionForRespondent1(CaseData caseData) {
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() != RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION);
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
    }
}
