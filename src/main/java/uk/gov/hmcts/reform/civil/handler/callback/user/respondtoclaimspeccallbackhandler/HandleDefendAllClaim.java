package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleDefendAllClaim implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String caseId = String.valueOf(caseData.getCcdCaseReference());

        log.info("Executing HandleDefendAllClaim for caseId: {}", caseId);

        List<String> errors = validatePaymentDate(caseData);
        if (!errors.isEmpty()) {
            log.warn("Validation failed for caseId: {}. Errors: {}", caseId, errors);
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCase = caseData.toBuilder();

        log.debug("Updating showConditionFlags for caseId: {}", caseId);
        updatedCase.showConditionFlags(whoDisputesFullDefence(caseData, caseId));
        log.debug("Updated showConditionFlags for caseId: {}", caseId);

        if (isDefendantResponseSpec(callbackParams)) {
            log.debug("Handling Defendant Response Spec for caseId: {}", caseId);
            handleDefendantResponseSpec(caseData, updatedCase, caseId);
            log.debug("Completed handling Defendant Response Spec for caseId: {}", caseId);
        }

        log.info("Successfully executed HandleDefendAllClaim for caseId: {}", caseId);
        return buildCallbackResponse(updatedCase);
    }

    private List<String> validatePaymentDate(CaseData caseData) {
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToClaim())
                                                 .orElseGet(() -> RespondToClaim.builder().build()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private boolean isDefendantResponseSpec(CallbackParams callbackParams) {
        return SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId());
    }

    private void handleDefendantResponseSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase, String caseId) {
        log.debug("Populating Respondent Response Type Spec Paid Status for caseId: {}", caseId);
        populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase, caseId);

        log.debug("Updating specPaidLessAmountOrDisputesOrPartAdmission for caseId: {}", caseId);
        updateSpecPaidLessAmountOrDisputesOrPartAdmission(caseData, updatedCase, caseId);

        log.debug("Updating specDisputesOrPartAdmission for caseId: {}", caseId);
        updateSpecDisputesOrPartAdmission(caseData, updatedCase, caseId);

        AllocatedTrack allocatedTrack = getAllocatedTrack(caseData);
        updatedCase.responseClaimTrack(allocatedTrack.name());
        log.debug("Set responseClaimTrack to {} for caseId: {}", allocatedTrack.name(), caseId);
    }

    private void updateSpecPaidLessAmountOrDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase, String caseId) {
        if (isPaidLessOrDisputesOrPartAdmission(caseData)) {
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(YES);
            log.debug("Set specPaidLessAmountOrDisputesOrPartAdmission to YES for caseId: {}", caseId);
        } else {
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(NO);
            log.debug("Set specPaidLessAmountOrDisputesOrPartAdmission to NO for caseId: {}", caseId);
        }
    }

    private boolean isPaidLessOrDisputesOrPartAdmission(CaseData caseData) {
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION;
    }

    private void updateSpecDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase, String caseId) {
        if (YES.equals(caseData.getIsRespondent2())) {
            if (isRespondent2DisputesOrPartAdmission(caseData)) {
                updatedCase.specDisputesOrPartAdmission(YES);
                log.debug("Set specDisputesOrPartAdmission to YES for respondent2, caseId: {}", caseId);
            } else {
                updatedCase.specDisputesOrPartAdmission(NO);
                log.debug("Set specDisputesOrPartAdmission to NO for respondent2, caseId: {}", caseId);
            }
        } else {
            if (isRespondent1DisputesOrPartAdmission(caseData)) {
                updatedCase.specDisputesOrPartAdmission(YES);
                log.debug("Set specDisputesOrPartAdmission to YES for respondent1, caseId: {}", caseId);
            } else {
                updatedCase.specDisputesOrPartAdmission(NO);
                log.debug("Set specDisputesOrPartAdmission to NO for respondent1, caseId: {}", caseId);
            }
        }
    }

    private boolean isRespondent2DisputesOrPartAdmission(CaseData caseData) {
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
            || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION);
    }

    private boolean isRespondent1DisputesOrPartAdmission(CaseData caseData) {
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
            && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
            || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION);
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedCase) {
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCase.build().toMap(objectMapper))
            .build();
    }

    private Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData, String caseId) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        log.debug("Removing dispute and payment-related tags for caseId: {}", caseId);
        removeWhoDisputesAndWhoPaidLess(tags, caseId);
        Set<DefendantResponseShowTag> bcoPartAdmission = whoDisputesBcoPartAdmission(caseData, caseId);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        switch (mpScenario) {
            case ONE_V_ONE:
                handleOneVOne(caseData, bcoPartAdmission, caseId);
                break;
            case TWO_V_ONE:
                handleTwoVOne(caseData, bcoPartAdmission, caseId);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRep(caseData, bcoPartAdmission, caseId);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRep(caseData, bcoPartAdmission, tags, caseId);
                break;
            default:
                log.error("Unsupported MultiPartyScenario: {} for caseId: {}", mpScenario, caseId);
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }

        tags.addAll(bcoPartAdmission);
        log.debug("Added BCO Part Admission tags for caseId: {}", caseId);
        addSomeoneDisputesTag(tags, caseId);
        return tags;
    }

    private void handleOneVOne(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling ONE_V_ONE scenario for caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for ONE_V_ONE scenario, caseId: {}", tag, caseId);
        });
    }

    private void handleTwoVOne(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling TWO_V_ONE scenario for caseId: {}", caseId);
        if (!bcoPartAdmission.contains(ONLY_RESPONDENT_1_DISPUTES)) {
            if (YES.equals(caseData.getDefendantSingleResponseToBothClaimants())) {
                handleSingleResponseToBothClaimants(caseData, bcoPartAdmission, caseId);
            } else {
                handleSeparateResponses(caseData, bcoPartAdmission, caseId);
            }
        }
    }

    private void handleOneVTwoOneLegalRep(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling ONE_V_TWO_ONE_LEGAL_REP scenario for caseId: {}", caseId);
        if (YES.equals(caseData.getRespondentResponseIsSame())) {
            handleSameResponse(caseData, bcoPartAdmission, caseId);
        } else {
            handleDifferentResponses(caseData, bcoPartAdmission, caseId);
        }
    }

    private void handleOneVTwoTwoLegalRep(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, Set<DefendantResponseShowTag> tags, String caseId) {
        log.debug("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseId);
        if (tags.contains(CAN_ANSWER_RESPONDENT_1)) {
            handleRespondent1(caseData, bcoPartAdmission, caseId);
        } else if (tags.contains(CAN_ANSWER_RESPONDENT_2)) {
            handleRespondent2(caseData, bcoPartAdmission, caseId);
        }
    }

    private void handleSingleResponseToBothClaimants(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling single response to both claimants for caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for single response to both claimants, caseId: {}", tag, caseId);
        });
    }

    private void handleSeparateResponses(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling separate responses for claimants in TWO_V_ONE scenario, caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getClaimant1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for separate response from claimant1, caseId: {}", tag, caseId);
        });

        fullDefenceAndPaidLess(
            caseData.getClaimant2ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for separate response from claimant2, caseId: {}", tag, caseId);
        });
    }

    private void handleSameResponse(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling same response from respondents in ONE_V_TWO_ONE_LEGAL_REP scenario, caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            BOTH_RESPONDENTS_DISPUTE,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for same response from respondents, caseId: {}", tag, caseId);
            if (tag == DefendantResponseShowTag.RESPONDENT_1_PAID_LESS) {
                bcoPartAdmission.add(DefendantResponseShowTag.RESPONDENT_2_PAID_LESS);
                log.debug("Added RESPONDENT_2_PAID_LESS for same response from respondents, caseId: {}", caseId);
            }
        });
    }

    private void handleDifferentResponses(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling different responses from respondents in ONE_V_TWO_ONE_LEGAL_REP scenario, caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for respondent1 different response, caseId: {}", tag, caseId);
        });

        if (YES.equals(caseData.getRespondentResponseIsSame())) {
            if (bcoPartAdmission.contains(DefendantResponseShowTag.RESPONDENT_1_PAID_LESS)) {
                bcoPartAdmission.add(DefendantResponseShowTag.RESPONDENT_2_PAID_LESS);
                log.debug("Added RESPONDENT_2_PAID_LESS as responses are same, caseId: {}", caseId);
            }
        } else {
            fullDefenceAndPaidLess(
                caseData.getRespondent2ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired2(),
                caseData.getRespondToClaim2(),
                caseData.getTotalClaimAmount(),
                DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
            ).ifPresent(tag -> {
                bcoPartAdmission.add(tag);
                log.debug("Added {} for respondent2 different response, caseId: {}", tag, caseId);
            });
        }

        EnumSet<DefendantResponseShowTag> bothOnlyDisputes = EnumSet.of(
            DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES
        );
        if (bcoPartAdmission.containsAll(bothOnlyDisputes)) {
            bcoPartAdmission.removeAll(bothOnlyDisputes);
            bcoPartAdmission.add(BOTH_RESPONDENTS_DISPUTE);
            log.debug("Set BOTH_RESPONDENTS_DISPUTE after removing individual disputes, caseId: {}", caseId);
        }
    }

    private void handleRespondent1(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling Respondent1 in ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for respondent1 in ONE_V_TWO_TWO_LEGAL_REP, caseId: {}", tag, caseId);
        });
    }

    private void handleRespondent2(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission, String caseId) {
        log.debug("Handling Respondent2 in ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseId);
        fullDefenceAndPaidLess(
            caseData.getRespondent2ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired2(),
            caseData.getRespondToClaim2(),
            caseData.getTotalClaimAmount(),
            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
        ).ifPresent(tag -> {
            bcoPartAdmission.add(tag);
            log.debug("Added {} for respondent2 in ONE_V_TWO_TWO_LEGAL_REP, caseId: {}", tag, caseId);
        });
    }

    private void addSomeoneDisputesTag(Set<DefendantResponseShowTag> tags, String caseId) {
        if (tags.contains(ONLY_RESPONDENT_1_DISPUTES)
            || tags.contains(ONLY_RESPONDENT_2_DISPUTES)
            || tags.contains(BOTH_RESPONDENTS_DISPUTE)) {
            tags.add(SOMEONE_DISPUTES);
            log.debug("Added SOMEONE_DISPUTES tag, caseId: {}", caseId);
        }
    }

    private void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated, String caseId) {
        log.debug("Updating Respondent1PaidStatus for caseId: {}", caseId);
        updateRespondent1PaidStatus(caseData, updated, caseId);
        if (YES.equals(caseData.getIsRespondent2())) {
            log.debug("Updating Respondent2PaidStatus for caseId: {}", caseId);
            updateRespondent2PaidStatus(caseData, updated, caseId);
        }
    }

    private void updateRespondent1PaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated, String caseId) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            setRespondent1PaidStatus(caseData, updated, caseId);
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY).build();
            log.debug("Set respondent1ClaimResponsePaymentAdmissionForSpec to DID_NOT_PAY for caseId: {}", caseId);
        }
    }

    private void setRespondent1PaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated, String caseId) {
        int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
            .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
        if (comparison < 0) {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            log.debug("Set respondent1ClaimResponsePaymentAdmissionForSpec to PAID_LESS_THAN_CLAIMED_AMOUNT for caseId: {}", caseId);
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            log.debug("Set respondent1ClaimResponsePaymentAdmissionForSpec to PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT for caseId: {}", caseId);
        }
    }

    private void updateRespondent2PaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated, String caseId) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
            && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
            setRespondent2PaidStatus(caseData, updated, caseId);
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
            log.debug("Set respondent1ClaimResponsePaymentAdmissionForSpec to null for caseId: {}", caseId);
        }
    }

    private void setRespondent2PaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated, String caseId) {
        int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
            .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
        if (comparison < 0) {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            log.debug("Set respondent1ClaimResponsePaymentAdmissionForSpec to PAID_LESS_THAN_CLAIMED_AMOUNT for respondent2, caseId: {}", caseId);
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            log.debug("Set respondent1ClaimResponsePaymentAdmissionForSpec to PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT for respondent2, caseId: {}", caseId);
        }
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null,
                                                toggleService, caseData);
    }

    private void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags, String caseId) {
        tags.removeIf(tag -> EnumSet.of(
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
            DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE,
            SOMEONE_DISPUTES,
            DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS,
            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS,
            DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID,
            DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL,
            DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL,
            DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1,
            DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2,
            DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            DefendantResponseShowTag.REPAYMENT_PLAN_2,
            DefendantResponseShowTag.MEDIATION
        ).contains(tag));
        log.debug("Removed dispute and payment-related tags for caseId: {}", caseId);
    }

    private Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData, String caseId) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        switch (mpScenario) {
            case ONE_V_ONE:
                handleOneVOnePartAdmission(caseData, tags, caseId);
                break;
            case TWO_V_ONE:
                handleTwoVOnePartAdmission(caseData, tags, caseId);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRepPartAdmission(caseData, tags, caseId);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRepPartAdmission(caseData, tags, caseId);
                break;
            default:
                log.error("Unsupported MultiPartyScenario: {} for caseId: {}", mpScenario, caseId);
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return tags;
    }

    private void handleOneVOnePartAdmission(CaseData caseData, Set<DefendantResponseShowTag> tags, String caseId) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
            log.debug("Added ONLY_RESPONDENT_1_DISPUTES for ONE_V_ONE scenario, caseId: {}", caseId);
        }
    }

    private void handleTwoVOnePartAdmission(CaseData caseData, Set<DefendantResponseShowTag> tags, String caseId) {
        if ((YES.equals(caseData.getDefendantSingleResponseToBothClaimants())
            && RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec())
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent1ClaimResponseTypeForSpec()
            || RespondentResponseTypeSpec.PART_ADMISSION == caseData.getRespondent2ClaimResponseTypeForSpec()) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
            log.debug("Added ONLY_RESPONDENT_1_DISPUTES for TWO_V_ONE scenario, caseId: {}", caseId);
        }
    }

    private void handleOneVTwoOneLegalRepPartAdmission(CaseData caseData, Set<DefendantResponseShowTag> tags, String caseId) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            if (YES.equals(caseData.getRespondentResponseIsSame())
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                tags.add(BOTH_RESPONDENTS_DISPUTE);
                log.debug("Added BOTH_RESPONDENTS_DISPUTE for ONE_V_TWO_ONE_LEGAL_REP scenario, caseId: {}", caseId);
            } else {
                tags.add(ONLY_RESPONDENT_1_DISPUTES);
                log.debug("Added ONLY_RESPONDENT_1_DISPUTES for ONE_V_TWO_ONE_LEGAL_REP scenario, caseId: {}", caseId);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_2_DISPUTES);
            log.debug("Added ONLY_RESPONDENT_2_DISPUTES for ONE_V_TWO_ONE_LEGAL_REP scenario, caseId: {}", caseId);
        }
    }

    private void handleOneVTwoTwoLegalRepPartAdmission(CaseData caseData, Set<DefendantResponseShowTag> tags, String caseId) {
        if (tags.contains(CAN_ANSWER_RESPONDENT_1)
            && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
            log.debug("Added ONLY_RESPONDENT_1_DISPUTES for ONE_V_TWO_TWO_LEGAL_REP scenario, caseId: {}", caseId);
        } else if (tags.contains(CAN_ANSWER_RESPONDENT_2)
            && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_2_DISPUTES);
            log.debug("Added ONLY_RESPONDENT_2_DISPUTES for ONE_V_TWO_TWO_LEGAL_REP scenario, caseId: {}", caseId);
        }
    }

    private Optional<DefendantResponseShowTag> fullDefenceAndPaidLess(
        RespondentResponseTypeSpec responseType,
        String fullDefenceRoute,
        RespondToClaim responseDetails,
        BigDecimal claimedAmount,
        DefendantResponseShowTag ifDisputing,
        DefendantResponseShowTag ifPaidLess) {

        if (FULL_DEFENCE == responseType) {
            if (isClaimDisputed(fullDefenceRoute)) {
                return Optional.ofNullable(ifDisputing);
            } else if (isPaidLessThanClaimed(responseDetails, claimedAmount)) {
                return Optional.ofNullable(ifPaidLess);
            }
        }
        return Optional.empty();
    }

    private boolean isClaimDisputed(String fullDefenceRoute) {
        return DISPUTES_THE_CLAIM.equals(fullDefenceRoute);
    }

    private boolean isPaidLessThanClaimed(RespondToClaim responseDetails, BigDecimal claimedAmount) {
        return Optional.ofNullable(responseDetails)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(MonetaryConversions::penniesToPounds)
            .map(wasPaid -> wasPaid.compareTo(claimedAmount) < 0)
            .orElse(false);
    }
}
