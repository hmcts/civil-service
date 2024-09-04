package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
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
    private final RespondToClaimSpecDocumentHandler respondToClaimSpecDocumentHandler;

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToClaim())
                                                                .orElseGet(() -> RespondToClaim.builder().build()));
        if (!errors.isEmpty()) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
        }
        CaseData.CaseDataBuilder<?, ?> updatedCase = caseData.toBuilder();
        updatedCase.showConditionFlags(whoDisputesFullDefence(caseData));
        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
             respondToClaimSpecDocumentHandler.populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase);
            if (caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(YES);
            } else {
                updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(NO);
            }
            if (YES.equals(caseData.getIsRespondent2())) {
                if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                    != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                    && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                    || caseData.getRespondent2ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION)) {
                    updatedCase.specDisputesOrPartAdmission(YES);
                } else {
                    updatedCase.specDisputesOrPartAdmission(NO);
                }
            } else {
                if (RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                    != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                    && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                    || caseData.getRespondent1ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION)) {
                    updatedCase.specDisputesOrPartAdmission(YES);
                } else {
                    updatedCase.specDisputesOrPartAdmission(NO);
                }
            }
            AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
            updatedCase.responseClaimTrack(allocatedTrack.name());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCase.build().toMap(objectMapper))
            .build();
    }

    public Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        return RespondToClaimSpecUtilsDisputeDetails.whoDisputesFullDefence(caseData);
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
    }
}
