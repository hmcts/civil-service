package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

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
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_1_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_1_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.RESPONDENT_2_PAID_LESS;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag.SOMEONE_DISPUTES;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleDefendAllClaim implements CaseTask {

    static final String UNKNOWN_MP_SCENARIO = "Unknown mp scenario";
    private final ObjectMapper objectMapper;
    private final FeatureToggleService toggleService;
    private final PaymentDateValidator paymentDateValidator;
    private final RespondToClaimSpecUtils respondToClaimSpecUtils;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        log.info("Executing callback for caseId: {}", caseData.getCcdCaseReference());

        List<String> errors = validatePaymentDate(caseData);
        if (!errors.isEmpty()) {
            log.debug("Validation errors found for caseId {}: {}", caseData.getCcdCaseReference(), errors);
            return buildErrorResponse(errors);
        }

        CaseData.CaseDataBuilder<?, ?> updatedCase = caseData.toBuilder();
        updatedCase.showConditionFlags(whoDisputesFullDefence(caseData));

        if (isDefendantResponseSpec(callbackParams)) {
            log.debug("Handling defendant response spec for caseId: {}", caseData.getCcdCaseReference());
            handleDefendantResponseSpec(caseData, updatedCase);
        }

        log.info("Callback execution completed for caseId: {}", caseData.getCcdCaseReference());
        return buildSuccessResponse(updatedCase.build());
    }

    private List<String> validatePaymentDate(CaseData caseData) {
        log.debug("Validating payment date for caseId: {}", caseData.getCcdCaseReference());
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToClaim())
                .orElseGet(() -> RespondToClaim.builder().build()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        log.error("Validation errors found: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    private boolean isDefendantResponseSpec(CallbackParams callbackParams) {
        log.debug("Checking if callback event is for defendant response spec: {} for caseId: {}",
                callbackParams.getRequest().getEventId(), callbackParams.getCaseData().getCcdCaseReference());
        return SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId());
    }

    private void handleDefendantResponseSpec(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase) {
        log.info("Handling defendant response spec for caseId: {}", caseData.getCcdCaseReference());
        populateRespondentResponseTypeSpecPaidStatus(caseData, updatedCase);
        updateSpecPaidLessAmountOrDisputesOrPartAdmission(caseData, updatedCase);
        updateSpecDisputesOrPartAdmission(caseData, updatedCase);
        updatedCase.responseClaimTrack(getAllocatedTrack(caseData).name());
    }

    private void updateSpecPaidLessAmountOrDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase) {
        log.info("Updating specPaidLessAmountOrDisputesOrPartAdmission for caseId: {}", caseData.getCcdCaseReference());

        if (isPaidLessOrDisputesOrPartAdmission(caseData)) {
            log.debug("CaseId {}: specPaidLessAmountOrDisputesOrPartAdmission set to YES", caseData.getCcdCaseReference());
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            log.debug("CaseId {}: specPaidLessAmountOrDisputesOrPartAdmission set to NO", caseData.getCcdCaseReference());
            updatedCase.specPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
    }

    private boolean isPaidLessOrDisputesOrPartAdmission(CaseData caseData) {
        log.debug("Checking if caseId {} has paid less or disputes or part admission", caseData.getCcdCaseReference());
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION;
    }

    private void updateSpecDisputesOrPartAdmission(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedCase) {
        log.info("Updating specDisputesOrPartAdmission for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2())) {
            if (isRespondent2DisputesOrPartAdmission(caseData)) {
                log.debug("CaseId {}: specDisputesOrPartAdmission set to YES for Respondent2", caseData.getCcdCaseReference());
                updatedCase.specDisputesOrPartAdmission(YES);
            } else {
                log.debug("CaseId {}: specDisputesOrPartAdmission set to NO for Respondent2", caseData.getCcdCaseReference());
                updatedCase.specDisputesOrPartAdmission(NO);
            }
        } else {
            if (isRespondent1DisputesOrPartAdmission(caseData)) {
                log.debug("CaseId {}: specDisputesOrPartAdmission set to YES for Respondent1", caseData.getCcdCaseReference());
                updatedCase.specDisputesOrPartAdmission(YES);
            } else {
                log.debug("CaseId {}: specDisputesOrPartAdmission set to NO for Respondent1", caseData.getCcdCaseReference());
                updatedCase.specDisputesOrPartAdmission(NO);
            }
        }
    }

    private boolean isRespondent2DisputesOrPartAdmission(CaseData caseData) {
        log.debug("Checking if Respondent2 disputes or part admission for caseId: {}", caseData.getCcdCaseReference());
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION);
    }

    private boolean isRespondent1DisputesOrPartAdmission(CaseData caseData) {
        log.debug("Checking if Respondent1 disputes or part admission for caseId: {}", caseData.getCcdCaseReference());
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION);
    }

    private CallbackResponse buildSuccessResponse(CaseData caseData) {
        log.info("Building success response for caseId: {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }

    private Set<DefendantResponseShowTag> whoDisputesFullDefence(CaseData caseData) {
        log.info("Determining who disputes full defence for caseId: {}", caseData.getCcdCaseReference());
        Set<DefendantResponseShowTag> tags = new HashSet<>(caseData.getShowConditionFlags());
        respondToClaimSpecUtils.removeWhoDisputesAndWhoPaidLess(tags);
        Set<DefendantResponseShowTag> bcoPartAdmission = respondToClaimSpecUtils.whoDisputesBcoPartAdmission(caseData);
        MultiPartyScenario mpScenario = getMultiPartyScenario(caseData);

        log.debug("MultiPartyScenario for caseId {}: {}", caseData.getCcdCaseReference(), mpScenario);

        switch (mpScenario) {
            case ONE_V_ONE:
                log.debug("Handling ONE_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
                handleOneVOne(caseData, bcoPartAdmission);
                break;
            case TWO_V_ONE:
                log.debug("Handling TWO_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
                handleTwoVOne(caseData, bcoPartAdmission);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                log.debug("Handling ONE_V_TWO_ONE_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());
                handleOneVTwoOneLegalRep(caseData, bcoPartAdmission);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                log.debug("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());
                handleOneVTwoTwoLegalRep(caseData, tags, bcoPartAdmission);
                break;
            default:
                log.error("Unknown MultiPartyScenario for caseId: {}", caseData.getCcdCaseReference());
                throw new UnsupportedOperationException(UNKNOWN_MP_SCENARIO);
        }

        tags.addAll(bcoPartAdmission);
        addSomeoneDisputesTag(tags);
        log.info("Completed determining who disputes full defence for caseId: {}", caseData.getCcdCaseReference());
        return tags;
    }

    private void handleOneVOne(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.debug("Handling ONE_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
        fullDefenceAndPaidLess(
                caseData.getRespondent1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_1_DISPUTES,
                DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);
    }

    private void handleTwoVOne(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling TWO_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());

        if (!bcoPartAdmission.contains(ONLY_RESPONDENT_1_DISPUTES)) {
            if (caseData.getDefendantSingleResponseToBothClaimants() == YES) {
                log.debug("CaseId {}: Handling single response to both claimants", caseData.getCcdCaseReference());
                handleSingleResponseToBothClaimants(caseData, bcoPartAdmission);
            } else {
                log.debug("CaseId {}: Handling separate responses", caseData.getCcdCaseReference());
                handleSeparateResponses(caseData, bcoPartAdmission);
            }
        }
    }

    private void handleSingleResponseToBothClaimants(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.debug("Handling single response to both claimants for caseId: {}", caseData.getCcdCaseReference());
        fullDefenceAndPaidLess(
                caseData.getRespondent1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_1_DISPUTES,
                DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);
    }

    private void handleSeparateResponses(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.debug("Handling separate responses for caseId: {}", caseData.getCcdCaseReference());
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

    private void handleOneVTwoOneLegalRep(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling ONE_V_TWO_ONE_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());

        if (caseData.getRespondentResponseIsSame() == YES) {
            log.debug("CaseId {}: Handling same response for both respondents", caseData.getCcdCaseReference());
            handleSameResponse(caseData, bcoPartAdmission);
        } else {
            log.debug("CaseId {}: Handling different responses for respondents", caseData.getCcdCaseReference());
            handleDifferentResponses(caseData, bcoPartAdmission);
        }
    }

    private void handleSameResponse(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.debug("Handling same response for caseId: {}", caseData.getCcdCaseReference());
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
    }

    private void handleDifferentResponses(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling different responses for caseId: {}", caseData.getCcdCaseReference());

        fullDefenceAndPaidLess(
                caseData.getRespondent1ClaimResponseTypeForSpec(),
                caseData.getDefenceRouteRequired(),
                caseData.getRespondToClaim(),
                caseData.getTotalClaimAmount(),
                ONLY_RESPONDENT_1_DISPUTES,
                DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
        ).ifPresent(bcoPartAdmission::add);

        if (caseData.getRespondentResponseIsSame() == YES) {
            log.debug("CaseId {}: Respondent responses are the same", caseData.getCcdCaseReference());
            if (bcoPartAdmission.contains(RESPONDENT_1_PAID_LESS)) {
                log.debug("Adding tag '{}' for Respondent2 for caseId: {}", RESPONDENT_2_PAID_LESS, caseData.getCcdCaseReference());
                bcoPartAdmission.add(RESPONDENT_2_PAID_LESS);
            }
        } else {
            log.debug("CaseId {}: Respondent responses are different", caseData.getCcdCaseReference());
            fullDefenceAndPaidLess(
                    caseData.getRespondent2ClaimResponseTypeForSpec(),
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
            log.debug("CaseId {}: Both respondents dispute, updating tags", caseData.getCcdCaseReference());
            bcoPartAdmission.removeAll(bothOnlyDisputes);
            bcoPartAdmission.add(BOTH_RESPONDENTS_DISPUTE);
        }
    }

    private void handleOneVTwoTwoLegalRep(CaseData caseData, Set<DefendantResponseShowTag> tags, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());

        if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)) {
            log.debug("CaseId {}: Handling response for Respondent1", caseData.getCcdCaseReference());
            fullDefenceAndPaidLess(
                    caseData.getRespondent1ClaimResponseTypeForSpec(),
                    caseData.getDefenceRouteRequired(),
                    caseData.getRespondToClaim(),
                    caseData.getTotalClaimAmount(),
                    ONLY_RESPONDENT_1_DISPUTES,
                    DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
            ).ifPresent(bcoPartAdmission::add);
        } else if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)) {
            log.debug("CaseId {}: Handling response for Respondent2", caseData.getCcdCaseReference());
            fullDefenceAndPaidLess(
                    caseData.getRespondent2ClaimResponseTypeForSpec(),
                    caseData.getDefenceRouteRequired2(),
                    caseData.getRespondToClaim2(),
                    caseData.getTotalClaimAmount(),
                    DefendantResponseShowTag.ONLY_RESPONDENT_2_DISPUTES,
                    DefendantResponseShowTag.RESPONDENT_2_PAID_LESS
            ).ifPresent(bcoPartAdmission::add);
        }
    }

    private void addSomeoneDisputesTag(Set<DefendantResponseShowTag> tags) {
        if (tags.contains(ONLY_RESPONDENT_1_DISPUTES)
                || tags.contains(ONLY_RESPONDENT_2_DISPUTES)
                || tags.contains(BOTH_RESPONDENTS_DISPUTE)) {
            tags.add(SOMEONE_DISPUTES);
        }
    }

    private void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated) {
        log.info("Populating RespondentResponseTypeSpecPaidStatus for caseId: {}", caseData.getCcdCaseReference());
        updateRespondent1PaymentStatus(caseData, updated);

        if (YES.equals(caseData.getIsRespondent2())) {
            log.debug("CaseId {}: Respondent2 is present, updating payment status", caseData.getCcdCaseReference());
            updateRespondent2PaymentStatus(caseData, updated);
        }
    }

    private void updateRespondent1PaymentStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated) {
        log.info("Updating Respondent1 payment status for caseId: {}", caseData.getCcdCaseReference());

        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
                && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                log.debug("CaseId {}: Respondent1 paid less than claimed amount", caseData.getCcdCaseReference());
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            } else {
                log.debug("CaseId {}: Respondent1 paid full or more than claimed amount", caseData.getCcdCaseReference());
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            }
        } else {
            log.debug("CaseId {}: Respondent1 did not pay", caseData.getCcdCaseReference());
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY).build();
        }
    }

    private void updateRespondent2PaymentStatus(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updated) {
        log.info("Updating Respondent2 payment status for caseId: {}", caseData.getCcdCaseReference());

        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
                && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
            int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                log.debug("CaseId {}: Respondent2 paid less than claimed amount", caseData.getCcdCaseReference());
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT).build();
            } else {
                log.debug("CaseId {}: Respondent2 paid full or more than claimed amount", caseData.getCcdCaseReference());
                updated.respondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT).build();
            }
        } else {
            log.debug("CaseId {}: Respondent2 did not pay", caseData.getCcdCaseReference());
            updated.respondent1ClaimResponsePaymentAdmissionForSpec(null).build();
        }
    }

    private AllocatedTrack getAllocatedTrack(CaseData caseData) {
        log.info("Determining allocated track for caseId: {}", caseData.getCcdCaseReference());
        return AllocatedTrack.getAllocatedTrack(caseData.getTotalClaimAmount(), null, null,
                toggleService, caseData);
    }

    private Optional<DefendantResponseShowTag> fullDefenceAndPaidLess(
            RespondentResponseTypeSpec responseType,
            String fullDefenceRoute,
            RespondToClaim responseDetails,
            BigDecimal claimedAmount,
            DefendantResponseShowTag ifDisputing,
            DefendantResponseShowTag ifPaidLess) {

        if (isFullDefence(responseType)) {
            if (isDisputingClaim(fullDefenceRoute)) {
                return Optional.ofNullable(ifDisputing);
            } else if (isPaidLessThanClaimed(responseDetails, claimedAmount)) {
                return Optional.ofNullable(ifPaidLess);
            }
        }
        return Optional.empty();
    }

    private boolean isFullDefence(RespondentResponseTypeSpec responseType) {
        return FULL_DEFENCE == responseType;
    }

    private boolean isDisputingClaim(String fullDefenceRoute) {
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
