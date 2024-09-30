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
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleDefendAllClaim implements CaseTask {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;
    private final RespondToClaimSpecUtils respondToClaimSpecUtilsDisputeDetails;

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";

    @Override
    public CallbackResponse execute(CallbackParams callbackParams) {
        log.info("Executing HandleDefendAllClaim with callbackParams: {}", callbackParams);
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = validatePayments(caseData);
        if (!errors.isEmpty()) {
            log.error("Validation errors found: {}", errors);
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCase = caseData.toBuilder();
        updatedCase.showConditionFlags(whoDisputesFullDefence(caseData));

        if (SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId())) {
            log.info("Populating respondent response type spec paid status");
            populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase);
            updateSpecPaidOrDisputeStatus(caseData, updatedCase);
            updatedCase.responseClaimTrack(getAllocatedTrack(caseData).name());
        }

        log.info("Building callback response");
        return buildCallbackResponse(updatedCase);
    }

    private List<String> validatePayments(CaseData caseData) {
        log.info("Validating payments for caseData: {}", caseData);
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToClaim())
                                                 .orElseGet(() -> RespondToClaim.builder().build()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        log.error("Building error response with errors: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder().errors(errors).build();
    }

    private void updateSpecPaidOrDisputeStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase) {
        log.info("Updating spec paid or dispute status for caseData: {}", caseData);
        if (isPaidLessOrDispute(caseData)) {
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }

        if (isSpecDisputeOrPartAdmission(caseData)) {
            updatedCase.specDisputesOrPartAdmission(YES);
        } else {
            updatedCase.specDisputesOrPartAdmission(NO);
        }
    }

    private boolean isPaidLessOrDispute(CaseData caseData) {
        return isPaidLessForRespondent1(caseData)
            || isDisputeForRespondent(caseData.getDefenceRouteRequired())
            || isDisputeForRespondent(caseData.getDefenceRouteRequired2())
            || isPartAdmissionForRespondent(caseData.getRespondent1ClaimResponseTypeForSpec())
            || isPartAdmissionForRespondent(caseData.getRespondent2ClaimResponseTypeForSpec());
    }

    private boolean isPaidLessForRespondent1(CaseData caseData) {
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT;
    }

    private boolean isDisputeForRespondent(String defenceRoute) {
        return DISPUTES_THE_CLAIM.equals(defenceRoute);
    }

    private boolean isPartAdmissionForRespondent(RespondentResponseTypeSpec responseType) {
        return responseType == RespondentResponseTypeSpec.PART_ADMISSION;
    }

    private boolean isSpecDisputeOrPartAdmission(CaseData caseData) {
        return YES.equals(caseData.getIsRespondent2())
            ? isSpecDisputeOrPartAdmissionForRespondent(caseData.getDefenceRouteRequired2(),
                                                        caseData.getRespondent2ClaimResponseTypeForSpec(), caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec())
            : isSpecDisputeOrPartAdmissionForRespondent(caseData.getDefenceRouteRequired(),
                                                        caseData.getRespondent1ClaimResponseTypeForSpec(), caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec());
    }

    private boolean isSpecDisputeOrPartAdmissionForRespondent(
        String defenceRoute, RespondentResponseTypeSpec responseType, RespondentResponseTypeSpecPaidStatus paymentStatus) {

        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != paymentStatus
            && (DISPUTES_THE_CLAIM.equals(defenceRoute) || responseType == RespondentResponseTypeSpec.PART_ADMISSION);
    }

    private Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        log.info("Determining who disputes full defence for caseData: {}", caseData);
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        respondToClaimSpecUtilsDisputeDetails.removeWhoDisputesAndWhoPaidLess(tags);

        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);
        Set<DefendantResponseShowTag> bcoPartAdmission = respondToClaimSpecUtilsDisputeDetails.whoDisputesBcoPartAdmission(caseData);

        handleScenario(mpScenario, caseData, tags, bcoPartAdmission);
        addSomeoneDisputesTag(tags);
        return tags;
    }

    private void handleScenario(MultiPartyScenario mpScenario, CaseData caseData,
                                Set<DefendantResponseShowTag> tags, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling scenario: {} for caseData: {}", mpScenario, caseData);
        switch (mpScenario) {
            case ONE_V_ONE:
                handleOneVOneScenario(caseData, bcoPartAdmission);
                break;
            case TWO_V_ONE:
                handleTwoVOneScenario(caseData, bcoPartAdmission);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                handleOneVTwoOneLegalRepScenario(caseData, bcoPartAdmission);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                handleOneVTwoTwoLegalRepScenario(caseData, tags, bcoPartAdmission);
                break;
            default:
                log.error("Unknown multi-party scenario: {}", mpScenario);
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }
    }

    private void handleOneVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling ONE_V_ONE scenario for caseData: {}", caseData);
        fullDefenceAndPaidLess(
            caseData.getRespondent1ClaimResponseTypeForSpec(),
            caseData.getDefenceRouteRequired(),
            caseData.getRespondToClaim(),
            caseData.getTotalClaimAmount(),
            ONLY_RESPONDENT_1_DISPUTES,
            RESPONDENT_1_PAID_LESS
        ).ifPresent(tags::add);
    }

    private void handleTwoVOneScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling TWO_V_ONE scenario for caseData: {}", caseData);
        if (YES.equals(caseData.getDefendantSingleResponseToBothClaimants())) {
            handleOneVOneScenario(caseData, tags);
        } else {
            fullDefenceAndPaidLess(
                caseData.getClaimant1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_1_DISPUTES,
                RESPONDENT_1_PAID_LESS
            ).ifPresent(tags::add);

            fullDefenceAndPaidLess(
                caseData.getClaimant2ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_1_DISPUTES,
                RESPONDENT_1_PAID_LESS
            ).ifPresent(tags::add);
        }
    }

    private void handleOneVTwoOneLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags) {
        log.info("Handling ONE_V_TWO_ONE_LEGAL_REP scenario for caseData: {}", caseData);
        if (YES.equals(caseData.getRespondentResponseIsSame())) {
            fullDefenceAndPaidLess(
                caseData.getRespondent1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                BOTH_RESPONDENTS_DISPUTE,
                RESPONDENT_1_PAID_LESS
            ).ifPresent(tag -> {
                tags.add(tag);
                if (tag == RESPONDENT_1_PAID_LESS) {
                    tags.add(RESPONDENT_2_PAID_LESS);
                }
            });
        } else {
            handleOneVOneScenario(caseData, tags);
        }
    }

    private void handleOneVTwoTwoLegalRepScenario(CaseData caseData, Set<DefendantResponseShowTag> tags, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseData: {}", caseData);
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

    private void addSomeoneDisputesTag(Set<DefendantResponseShowTag> tags) {
        log.info("Adding SOMEONE_DISPUTES tag if applicable");
        if (tags.contains(ONLY_RESPONDENT_1_DISPUTES)
            || tags.contains(ONLY_RESPONDENT_2_DISPUTES)
            || tags.contains(BOTH_RESPONDENTS_DISPUTE)) {
            tags.add(SOMEONE_DISPUTES);
        }
    }

    private Optional<DefendantResponseShowTag> fullDefenceAndPaidLess(
        RespondentResponseTypeSpec responseType,
        String fullDefenceRoute,
        RespondToClaim responseDetails,
        BigDecimal claimedAmount,
        DefendantResponseShowTag ifDisputing,
        DefendantResponseShowTag ifPaidLess) {

        if (isFullDefence(responseType)) {
            return getDefenceOrPaidLessTag(fullDefenceRoute, responseDetails, claimedAmount, ifDisputing, ifPaidLess);
        }

        return Optional.empty();
    }

    private boolean isFullDefence(RespondentResponseTypeSpec responseType) {
        return FULL_DEFENCE == responseType;
    }

    private Optional<DefendantResponseShowTag> getDefenceOrPaidLessTag(
        String fullDefenceRoute, RespondToClaim responseDetails, BigDecimal claimedAmount,
        DefendantResponseShowTag ifDisputing, DefendantResponseShowTag ifPaidLess) {

        if (isDisputing(fullDefenceRoute)) {
            return Optional.ofNullable(ifDisputing);
        } else if (isPaidLess(responseDetails, claimedAmount)) {
            return Optional.ofNullable(ifPaidLess);
        }

        return Optional.empty();
    }

    private boolean isDisputing(String fullDefenceRoute) {
        return DISPUTES_THE_CLAIM.equals(fullDefenceRoute);
    }

    private boolean isPaidLess(RespondToClaim responseDetails, BigDecimal claimedAmount) {
        return Optional.ofNullable(responseDetails)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(MonetaryConversions::penniesToPounds)
            .map(wasPaid -> wasPaid.compareTo(claimedAmount) < 0)
            .orElse(false);
    }

    private void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated) {
        log.info("Populating respondent response type spec paid status for caseData: {}", caseData);
        setRespondentPaidStatus(caseData, caseData.getDefenceRouteRequired(), caseData.getRespondToClaim(),
                                updated::respondent1ClaimResponsePaymentAdmissionForSpec);

        if (YES.equals(caseData.getIsRespondent2())) {
            setRespondentPaidStatus(caseData, caseData.getDefenceRouteRequired2(), caseData.getRespondToClaim2(),
                                    updated::respondent1ClaimResponsePaymentAdmissionForSpec);
        }
    }

    private void setRespondentPaidStatus(CaseData caseData, String defenceRoute, RespondToClaim respondToClaim,
                                         java.util.function.Consumer<RespondentResponseTypeSpecPaidStatus> updatePaidStatus) {
        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(defenceRoute)
            && respondToClaim.getHowMuchWasPaid() != null) {
            int comparison = respondToClaim.getHowMuchWasPaid()
                .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                updatePaidStatus.accept(RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT);
            } else {
                updatePaidStatus.accept(RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT);
            }
        } else {
            updatePaidStatus.accept(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY);
        }
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        log.info("Getting allocated track for caseData: {}", caseData);
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null, toggleService, caseData);
    }

    private CallbackResponse buildCallbackResponse(CaseData.CaseDataBuilder<?, ?> updatedCaseData) {
        log.info("Building callback response for updated case data");
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }
}
