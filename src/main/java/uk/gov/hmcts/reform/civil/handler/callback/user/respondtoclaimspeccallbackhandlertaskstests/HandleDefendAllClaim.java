package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

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
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.NEED_FINANCIAL_DETAILS_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.REPAYMENT_PLAN_2;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_ADMITS_PART_OR_FULL;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHEN_WILL_CLAIM_BE_PAID;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.WHY_2_DOES_NOT_PAY_IMMEDIATELY;

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
            populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase);
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

    private Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        // in case of backtracking
        removeWhoDisputesAndWhoPaidLess(tags);
        Set<DefendantResponseShowTag> bcoPartAdmission = whoDisputesBcoPartAdmission(caseData);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        switch (mpScenario) {
            case ONE_V_ONE:
                fullDefenceAndPaidLess(
                    caseData.getRespondent1ClaimResponseTypeForSpec(),
                    caseData.getDefenceRouteRequired(),
                    caseData.getRespondToClaim(),
                    caseData.getTotalClaimAmount(),
                    ONLY_RESPONDENT_1_DISPUTES,
                    DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                ).ifPresent(bcoPartAdmission::add);
                break;
            case TWO_V_ONE:
                if (!bcoPartAdmission.contains(ONLY_RESPONDENT_1_DISPUTES)) {
                    if (caseData.getDefendantSingleResponseToBothClaimants() == YES) {
                        fullDefenceAndPaidLess(
                            caseData.getRespondent1ClaimResponseTypeForSpec(),
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            ONLY_RESPONDENT_1_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);
                    } else {
                        fullDefenceAndPaidLess(
                            caseData.getClaimant1ClaimResponseTypeForSpec(),
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            ONLY_RESPONDENT_1_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);

                        fullDefenceAndPaidLess(
                            caseData.getClaimant2ClaimResponseTypeForSpec(),
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            ONLY_RESPONDENT_1_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);
                    }
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (caseData.getRespondentResponseIsSame() == YES) {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent1ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired(),
                        caseData.getRespondToClaim(),
                        caseData.getTotalClaimAmount(),
                        BOTH_RESPONDENTS_DISPUTE,
                        DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                    ).ifPresent(tag -> {
                        bcoPartAdmission.add(tag);
                        if (tag == DefendantResponseShowTag.RESPONDENT_1_PAID_LESS) {
                            bcoPartAdmission.add(DefendantResponseShowTag.RESPONDENT_2_PAID_LESS);
                        }
                    });
                } else {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent1ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired(),
                        caseData.getRespondToClaim(),
                        caseData.getTotalClaimAmount(),
                        ONLY_RESPONDENT_1_DISPUTES,
                        DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                    ).ifPresent(bcoPartAdmission::add);
                    if (caseData.getRespondentResponseIsSame() == YES) {
                        if (bcoPartAdmission.contains(RESPONDENT_1_PAID_LESS)) {
                            bcoPartAdmission.add(RESPONDENT_2_PAID_LESS);
                        }
                    } else {
                        fullDefenceAndPaidLess(
                            caseData.getRespondent2ClaimResponseTypeForSpec(),
                            // if only 2nd defends, defenceRouteRequired2 field is not used
                            caseData.getDefenceRouteRequired(),
                            caseData.getRespondToClaim(),
                            caseData.getTotalClaimAmount(),
                            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
                        ).ifPresent(bcoPartAdmission::add);
                    }
                    EnumSet<DefendantResponseShowTag> bothOnlyDisputes = EnumSet.of(
                        DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES,
                        DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES
                    );
                    if (bcoPartAdmission.containsAll(bothOnlyDisputes)) {
                        bcoPartAdmission.removeAll(bothOnlyDisputes);
                        bcoPartAdmission.add(BOTH_RESPONDENTS_DISPUTE);
                    }
                }
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)) {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent1ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired(),
                        caseData.getRespondToClaim(),
                        caseData.getTotalClaimAmount(),
                        ONLY_RESPONDENT_1_DISPUTES,
                        DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
                    ).ifPresent(bcoPartAdmission::add);
                } else if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)) {
                    fullDefenceAndPaidLess(
                        caseData.getRespondent2ClaimResponseTypeForSpec(),
                        caseData.getDefenceRouteRequired2(),
                        caseData.getRespondToClaim2(),
                        caseData.getTotalClaimAmount(),
                        DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                        DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
                    ).ifPresent(bcoPartAdmission::add);
                }
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        tags.addAll(bcoPartAdmission);
        if (tags.contains(ONLY_RESPONDENT_1_DISPUTES)
            || tags.contains(ONLY_RESPONDENT_2_DISPUTES)
            || tags.contains(BOTH_RESPONDENTS_DISPUTE)) {
            tags.add(SOMEONE_DISPUTES);
        }
        return tags;
    }

    private void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData,
                                                              CaseData.CaseDataBuilder<?, ?> updated) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
            && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
            int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
                .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                    RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            } else {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                    RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            }
        } else {
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY)
                .build();
        }

        if (YES.equals(caseData.getIsRespondent2())) {
            if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
                && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
                // CIV-208 howMuchWasPaid is pence, totalClaimAmount is pounds, hence the need for conversion
                int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
                if (comparison < 0) {
                    updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
                } else {
                    updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
                }
            } else {
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
            }
        }
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null,
                                                toggleService, caseData);
    }

    private void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
        tags.removeIf(EnumSet.of(
            ONLY_RESPONDENT_1_DISPUTES,
            DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
            DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE,
            SOMEONE_DISPUTES,
            DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL,
            DefendantResponseShowTag.RESPONDENT_1_PAID_LESS,
            DefendantResponseShowTag.RESPONDENT_2_PAID_LESS,
            WHEN_WILL_CLAIM_BE_PAID,
            RESPONDENT_1_ADMITS_PART_OR_FULL,
            RESPONDENT_2_ADMITS_PART_OR_FULL,
            NEED_FINANCIAL_DETAILS_1,
            NEED_FINANCIAL_DETAILS_2,
            DefendantResponseShowTag.WHY_1_DOES_NOT_PAY_IMMEDIATELY,
            WHY_2_DOES_NOT_PAY_IMMEDIATELY,
            REPAYMENT_PLAN_2,
            DefendantResponseShowTag.MEDIATION
        )::contains);
    }

    private Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        switch (mpScenario) {
            case ONE_V_ONE:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case TWO_V_ONE:
                if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION)
                    || caseData.getClaimant1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                    || caseData.getClaimant2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    if (caseData.getRespondentResponseIsSame() == YES
                        || caseData.getRespondent2ClaimResponseTypeForSpec()
                        == RespondentResponseTypeSpec.PART_ADMISSION) {
                        tags.add(DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE);
                    } else {
                        tags.add(ONLY_RESPONDENT_1_DISPUTES);
                    }
                } else if (caseData.getRespondent2ClaimResponseTypeForSpec()
                    == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
                }
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                } else if (caseData.getShowConditionFlags().contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)
                    && caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
                    tags.add(DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES);
                }
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return tags;
    }

    private Optional<DefendantResponseShowTag> fullDefenceAndPaidLess(
        RespondentResponseTypeSpec responseType,
        String fullDefenceRoute,
        RespondToClaim responseDetails,
        BigDecimal claimedAmount,
        DefendantResponseShowTag ifDisputing,
        DefendantResponseShowTag ifPaidLess) {
        if (FULL_DEFENCE == responseType) {
            if (DISPUTES_THE_CLAIM.equals(fullDefenceRoute)) {
                return Optional.ofNullable(ifDisputing);
            } else if (Optional.ofNullable(responseDetails)
                .map(RespondToClaim::getHowMuchWasPaid)
                .map(MonetaryConversions::penniesToPounds)
                .map(wasPaid1 -> wasPaid1.compareTo(claimedAmount) < 0)
                .orElse(false)) {
                return Optional.ofNullable(ifPaidLess);
            }
        }
        return Optional.empty();
    }
}
