package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.BOTH_RESPONDENTS_DISPUTE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2;
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
public class RespondToClaimSpecUtilsDisputeDetails {

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    public static final String UNAVAILABLE_DATE_RANGE_MISSING = "Please provide at least one valid Date from if you cannot attend hearing within next 3 months.";
    public static final String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after Unavailability Date to. Please enter valid range.";
    public static final String INVALID_UNAVAILABLE_DATE_BEFORE_TODAY = "Unavailability date must not be before today.";
    public static final String INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY = "Unavailability date from must not be before today.";
    public static final String INVALID_UNAVAILABLE_DATE_TO_WHEN_MORE_THAN_YEAR = "Unavailability date to must not be more than one year in the future.";
    public static final String INVALID_UNAVAILABLE_DATE_WHEN_MORE_THAN_YEAR = "Unavailability date must not be more than one year in the future.";

    public static void checkUnavailable(List<String> errors, List<Element<UnavailableDate>> datesUnavailableList) {
        if (isEmpty(datesUnavailableList)) {
            errors.add(UNAVAILABLE_DATE_RANGE_MISSING);
        } else {
            for (Element<UnavailableDate> dateRange : datesUnavailableList) {
                LocalDate dateFrom = dateRange.getValue().getFromDate();
                LocalDate dateTo = dateRange.getValue().getToDate();
                UnavailableDateType dateType = dateRange.getValue().getUnavailableDateType();

                if (dateType == UnavailableDateType.SINGLE_DATE) {
                    validateSingleDate(errors, dateRange.getValue().getDate());
                } else if (dateType == UnavailableDateType.DATE_RANGE) {
                    validateDateRange(errors, dateFrom, dateTo);
                }
            }
        }
    }

    private static void validateSingleDate(List<String> errors, LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            errors.add(INVALID_UNAVAILABLE_DATE_BEFORE_TODAY);
        } else if (date.isAfter(LocalDate.now().plusYears(1))) {
            errors.add(INVALID_UNAVAILABLE_DATE_WHEN_MORE_THAN_YEAR);
        }
    }

    private static void validateDateRange(List<String> errors, LocalDate dateFrom, LocalDate dateTo) {
        if (dateTo != null && dateTo.isBefore(dateFrom)) {
            errors.add(INVALID_UNAVAILABILITY_RANGE);
        } else if (dateFrom != null && dateFrom.isBefore(LocalDate.now())) {
            errors.add(INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY);
        } else if (dateTo != null && dateTo.isAfter(LocalDate.now().plusYears(1))) {
            errors.add(INVALID_UNAVAILABLE_DATE_TO_WHEN_MORE_THAN_YEAR);
        }
    }

    public static Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        removeWhoDisputesAndWhoPaidLess(tags);
        Set<DefendantResponseShowTag> bcoPartAdmission = whoDisputesBcoPartAdmission(caseData);

        switch (getMultiPartyScenario(caseData)) {
            case ONE_V_ONE -> handleOneVOne(caseData, bcoPartAdmission);
            case TWO_V_ONE -> handleTwoVOne(caseData, bcoPartAdmission);
            case ONE_V_TWO_ONE_LEGAL_REP -> handleOneVTwoOneLegalRep(caseData, bcoPartAdmission);
            case ONE_V_TWO_TWO_LEGAL_REP -> handleOneVTwoTwoLegalRep(caseData, tags, bcoPartAdmission);
            default -> throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }

        tags.addAll(bcoPartAdmission);
        if (tags.contains(ONLY_RESPONDENT_1_DISPUTES)
            || tags.contains(ONLY_RESPONDENT_2_DISPUTES)
            || tags.contains(BOTH_RESPONDENTS_DISPUTE)) {
            tags.add(SOMEONE_DISPUTES);
        }
        return tags;
    }

    private static void handleOneVOne(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);
    }

    private static void handleTwoVOne(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        if (!bcoPartAdmission.contains(ONLY_RESPONDENT_1_DISPUTES)) {
            if (caseData.getDefendantSingleResponseToBothClaimants() == YES) {
                fullDefenceAndPaidLess(
                    caseData.getRespondent1ClaimResponseTypeForSpec(),
                    caseData.getDefenceRouteRequired(),
                    caseData.getRespondToClaim(),
                    caseData.getTotalClaimAmount(),
                    ONLY_RESPONDENT_1_DISPUTES,
                    RESPONDENT_1_PAID_LESS
                ).ifPresent(bcoPartAdmission::add);
            } else {
                handleTwoVOneSeparateResponses(caseData, bcoPartAdmission);
            }
        }
    }

    private static void handleTwoVOneSeparateResponses(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        fullDefenceAndPaidLess(
            caseData.getClaimant1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);

        fullDefenceAndPaidLess(
            caseData.getClaimant2ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);
    }

    private static void handleOneVTwoOneLegalRep(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        if (caseData.getRespondentResponseIsSame() == YES) {
            fullDefenceAndPaidLess(
                caseData.getRespondent1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                BOTH_RESPONDENTS_DISPUTE,
                RESPONDENT_1_PAID_LESS
            ).ifPresent(tag -> {
                bcoPartAdmission.add(tag);
                if (tag == RESPONDENT_1_PAID_LESS) {
                    bcoPartAdmission.add(RESPONDENT_2_PAID_LESS);
                }
            });
        } else {
            handleOneVTwoOneLegalRepSeparateResponses(caseData, bcoPartAdmission);
        }
    }

    private static void handleOneVTwoOneLegalRepSeparateResponses(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);

        if (caseData.getRespondentResponseIsSame() == YES && bcoPartAdmission.contains(RESPONDENT_1_PAID_LESS)) {
            bcoPartAdmission.add(RESPONDENT_2_PAID_LESS);
        } else {
            fullDefenceAndPaidLess(
                caseData.getRespondent2ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_2_DISPUTES,
                RESPONDENT_2_PAID_LESS
            ).ifPresent(bcoPartAdmission::add);
        }

        if (bcoPartAdmission.containsAll(EnumSet.of(ONLY_RESPONDENT_1_DISPUTES, ONLY_RESPONDENT_2_DISPUTES))) {
            bcoPartAdmission.removeAll(EnumSet.of(ONLY_RESPONDENT_1_DISPUTES, ONLY_RESPONDENT_2_DISPUTES));
            bcoPartAdmission.add(BOTH_RESPONDENTS_DISPUTE);
        }
    }

    private static void handleOneVTwoTwoLegalRep(CaseData caseData, Set<DefendantResponseShowTag> tags, Set<DefendantResponseShowTag> bcoPartAdmission) {
        if (tags.contains(CAN_ANSWER_RESPONDENT_1)) {
            fullDefenceAndPaidLess(
                caseData.getRespondent1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_1_DISPUTES,
                RESPONDENT_1_PAID_LESS
            ).ifPresent(bcoPartAdmission::add);
        } else if (tags.contains(CAN_ANSWER_RESPONDENT_2)) {
            fullDefenceAndPaidLess(
                caseData.getRespondent2ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired2(),
                caseData.getRespondToClaim2(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_2_DISPUTES,
                RESPONDENT_2_PAID_LESS
            ).ifPresent(bcoPartAdmission::add);
        }
    }

    public static Set<DefendantResponseShowTag> whoDisputesPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        removeWhoDisputesAndWhoPaidLess(tags);
        tags.addAll(whoDisputesBcoPartAdmission(caseData));
        return tags;
    }

    public static boolean mustWhenWillClaimBePaidBeShown(CaseData caseData) {
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)) {
            return caseData.getSpecDefenceFullAdmittedRequired() == NO || caseData.getSpecDefenceAdmittedRequired() == NO;
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)) {
            return caseData.getSpecDefenceFullAdmitted2Required() == NO || caseData.getSpecDefenceAdmitted2Required() == NO;
        }
        return false;
    }

    static boolean respondent1doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        return YES.equals(caseData.getIsRespondent1())
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && (scenario != ONE_V_TWO_ONE_LEGAL_REP || caseData.getRespondentResponseIsSame() == YES)
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
            && caseData.getSpecDefenceFullAdmittedRequired() != YES
            && caseData.getSpecDefenceAdmittedRequired() != YES;
    }

    static boolean needFinancialInfo21v2ds(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
            && caseData.getSpecDefenceAdmitted2Required() != YES
            && caseData.getSpecDefenceFullAdmitted2Required() != YES
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != RespondentResponseTypeSpec.COUNTER_CLAIM;
    }

    static boolean respondent2doesNotPayImmediately(CaseData caseData, MultiPartyScenario scenario) {
        if (caseData.getRespondentClaimResponseTypeForSpecGeneric() != COUNTER_CLAIM
            && caseData.getRespondentClaimResponseTypeForSpecGeneric() != FULL_DEFENCE) {
            if (scenario == ONE_V_TWO_ONE_LEGAL_REP && caseData.getRespondentResponseIsSame() == YES) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmittedRequired() != YES
                    && caseData.getSpecDefenceAdmittedRequired() != YES;
            } else if (caseData.getRespondentResponseIsSame() != null || scenario == ONE_V_TWO_TWO_LEGAL_REP) {
                return caseData.getDefenceAdmitPartPaymentTimeRouteRequired2() != IMMEDIATELY
                    && caseData.getSpecDefenceFullAdmitted2Required() != YES
                    && caseData.getSpecDefenceAdmitted2Required() != YES;
            }
        }
        return false;
    }

    static boolean respondent2HasSameLegalRep(CaseData caseData) {
        return YES.equals(caseData.getRespondent2SameLegalRepresentative());
    }

    public static void removeWhoDisputesAndWhoPaidLess(Set<DefendantResponseShowTag> tags) {
        tags.removeIf(EnumSet.of(
            ONLY_RESPONDENT_1_DISPUTES,
            ONLY_RESPONDENT_2_DISPUTES,
            BOTH_RESPONDENTS_DISPUTE,
            SOMEONE_DISPUTES,
            DefendantResponseShowTag.CURRENT_ADMITS_PART_OR_FULL,
            RESPONDENT_1_PAID_LESS,
            RESPONDENT_2_PAID_LESS,
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

    public static Set<DefendantResponseShowTag> whoDisputesBcoPartAdmission(CaseData caseData) {
        Set<DefendantResponseShowTag> tags = EnumSet.noneOf(DefendantResponseShowTag.class);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        switch (mpScenario) {
            case ONE_V_ONE:
                if (caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case TWO_V_ONE:
                if ((caseData.getDefendantSingleResponseToBothClaimants() == YES
                    && caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION)
                    || caseData.getClaimant1ClaimResponseTypeForSpec() == PART_ADMISSION
                    || caseData.getClaimant2ClaimResponseTypeForSpec() == PART_ADMISSION) {
                    tags.add(ONLY_RESPONDENT_1_DISPUTES);
                }
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRepPartAdmission(caseData, tags);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRepPartAdmission(caseData, tags);
                break;
            default:
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
        return tags;
    }

    private static void handleOneVTwoOneLegalRepPartAdmission(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            if (caseData.getRespondentResponseIsSame() == YES || caseData.getRespondent2ClaimResponseTypeForSpec() == PART_ADMISSION) {
                tags.add(BOTH_RESPONDENTS_DISPUTE);
            } else {
                tags.add(ONLY_RESPONDENT_1_DISPUTES);
            }
        } else if (caseData.getRespondent2ClaimResponseTypeForSpec() == PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    private static void handleOneVTwoTwoLegalRepPartAdmission(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_1)
            && caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_1_DISPUTES);
        } else if (caseData.getShowConditionFlags().contains(CAN_ANSWER_RESPONDENT_2)
            && caseData.getRespondent2ClaimResponseTypeForSpec() == PART_ADMISSION) {
            tags.add(ONLY_RESPONDENT_2_DISPUTES);
        }
    }

    public static Optional<DefendantResponseShowTag> fullDefenceAndPaidLess(
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

    static boolean someoneDisputes(CaseData caseData) {
        if (TWO_V_ONE.equals(getMultiPartyScenario(caseData))) {
            return ((caseData.getClaimant1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getClaimant2ClaimResponseTypeForSpec() == FULL_DEFENCE)
                || caseData.getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE
                || caseData.getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION);
        } else {
            return someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_1,
                                   caseData.getRespondent1ClaimResponseTypeForSpec()
            )
                || someoneDisputes(caseData, CAN_ANSWER_RESPONDENT_2,
                                   caseData.getRespondent2ClaimResponseTypeForSpec()
            );
        }
    }

    private static boolean someoneDisputes(CaseData caseData, DefendantResponseShowTag respondent, RespondentResponseTypeSpec response) {
        return caseData.getShowConditionFlags().contains(respondent)
            && (response == FULL_DEFENCE
            || (response == PART_ADMISSION && !NO.equals(caseData.getRespondentResponseIsSame())));
    }
}
