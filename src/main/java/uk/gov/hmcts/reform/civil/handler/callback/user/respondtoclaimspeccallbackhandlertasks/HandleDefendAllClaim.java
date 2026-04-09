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
            log.info("Validation errors found for caseId {}: {}", caseData.getCcdCaseReference(), errors);
            return buildErrorResponse(errors);
        }

        caseData.setShowConditionFlags(whoDisputesFullDefence(caseData));

        if (isDefendantResponseSpec(callbackParams)) {
            log.info("Handling defendant response spec for caseId: {}", caseData.getCcdCaseReference());
            handleDefendantResponseSpec(caseData);
        }

        log.info("Callback execution completed for caseId: {}", caseData.getCcdCaseReference());
        return buildSuccessResponse(caseData);
    }

    private List<String> validatePaymentDate(CaseData caseData) {
        log.info("Validating payment date for caseId: {}", caseData.getCcdCaseReference());
        return paymentDateValidator.validate(Optional.ofNullable(caseData.getRespondToClaim())
                .orElseGet(() -> new RespondToClaim()));
    }

    private CallbackResponse buildErrorResponse(List<String> errors) {
        log.error("Validation errors found: {}", errors);
        return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errors)
                .build();
    }

    private boolean isDefendantResponseSpec(CallbackParams callbackParams) {
        log.info("Checking if callback event is for defendant response spec: {} for caseId: {}",
                callbackParams.getRequest().getEventId(), callbackParams.getCaseData().getCcdCaseReference());
        return SpecJourneyConstantLRSpec.DEFENDANT_RESPONSE_SPEC.equals(callbackParams.getRequest().getEventId());
    }

    private void handleDefendantResponseSpec(CaseData caseData) {
        log.info("Handling defendant response spec for caseId: {}", caseData.getCcdCaseReference());
        populateRespondentResponseTypeSpecPaidStatus(caseData);
        updateSpecPaidLessAmountOrDisputesOrPartAdmission(caseData);
        updateSpecDisputesOrPartAdmission(caseData);
        caseData.setResponseClaimTrack(getAllocatedTrack(caseData).name());
    }

    private void updateSpecPaidLessAmountOrDisputesOrPartAdmission(CaseData caseData) {
        log.info("Updating specPaidLessAmountOrDisputesOrPartAdmission for caseId: {}", caseData.getCcdCaseReference());

        if (isPaidLessOrDisputesOrPartAdmission(caseData)) {
            log.info("CaseId {}: specPaidLessAmountOrDisputesOrPartAdmission set to YES", caseData.getCcdCaseReference());
            caseData.setSpecPaidLessAmountOrDisputesOrPartAdmission(YES);
        } else {
            log.info("CaseId {}: specPaidLessAmountOrDisputesOrPartAdmission set to NO", caseData.getCcdCaseReference());
            caseData.setSpecPaidLessAmountOrDisputesOrPartAdmission(NO);
        }
    }

    private boolean isPaidLessOrDisputesOrPartAdmission(CaseData caseData) {
        log.info("Checking if caseId {} has paid less or disputes or part admission", caseData.getCcdCaseReference());
        return caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec() == RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired())
                || DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION;
    }

    private void updateSpecDisputesOrPartAdmission(CaseData caseData) {
        log.info("Updating specDisputesOrPartAdmission for caseId: {}", caseData.getCcdCaseReference());

        if (YES.equals(caseData.getIsRespondent2())) {
            if (isRespondent2DisputesOrPartAdmission(caseData)) {
                log.info("CaseId {}: specDisputesOrPartAdmission set to YES for Respondent2", caseData.getCcdCaseReference());
                caseData.setSpecDisputesOrPartAdmission(YES);
            } else {
                log.info("CaseId {}: specDisputesOrPartAdmission set to NO for Respondent2", caseData.getCcdCaseReference());
                caseData.setSpecDisputesOrPartAdmission(NO);
            }
        } else {
            if (isRespondent1DisputesOrPartAdmission(caseData)) {
                log.info("CaseId {}: specDisputesOrPartAdmission set to YES for Respondent1", caseData.getCcdCaseReference());
                caseData.setSpecDisputesOrPartAdmission(YES);
            } else {
                log.info("CaseId {}: specDisputesOrPartAdmission set to NO for Respondent1", caseData.getCcdCaseReference());
                caseData.setSpecDisputesOrPartAdmission(NO);
            }
        }
    }

    private boolean isRespondent2DisputesOrPartAdmission(CaseData caseData) {
        log.info("Checking if Respondent2 disputes or part admission for caseId: {}", caseData.getCcdCaseReference());
        return RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT != caseData.getRespondent1ClaimResponsePaymentAdmissionForSpec()
                && (DISPUTES_THE_CLAIM.equals(caseData.getDefenceRouteRequired2())
                || caseData.getRespondent2ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION);
    }

    private boolean isRespondent1DisputesOrPartAdmission(CaseData caseData) {
        log.info("Checking if Respondent1 disputes or part admission for caseId: {}", caseData.getCcdCaseReference());
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

        log.info("MultiPartyScenario for caseId {}: {}", caseData.getCcdCaseReference(), mpScenario);

        switch (mpScenario) {
            case ONE_V_ONE:
                log.info("Handling ONE_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
                handleOneVOne(caseData, bcoPartAdmission);
                break;
            case TWO_V_ONE:
                log.info("Handling TWO_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
                handleTwoVOne(caseData, bcoPartAdmission);
                break;
            case ONE_V_TWO_ONE_LEGAL_REP:
                log.info("Handling ONE_V_TWO_ONE_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());
                handleOneVTwoOneLegalRep(caseData, bcoPartAdmission);
                break;
            case ONE_V_TWO_TWO_LEGAL_REP:
                log.info("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());
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
        log.info("Handling ONE_V_ONE scenario for caseId: {}", caseData.getCcdCaseReference());
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
                log.info("CaseId {}: Handling single response to both claimants", caseData.getCcdCaseReference());
                handleSingleResponseToBothClaimants(caseData, bcoPartAdmission);
            } else {
                log.info("CaseId {}: Handling separate responses", caseData.getCcdCaseReference());
                handleSeparateResponses(caseData, bcoPartAdmission);
            }
        }
    }

    private void handleSingleResponseToBothClaimants(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling single response to both claimants for caseId: {}", caseData.getCcdCaseReference());
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
        log.info("Handling separate responses for caseId: {}", caseData.getCcdCaseReference());
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
            log.info("CaseId {}: Handling same response for both respondents", caseData.getCcdCaseReference());
            handleSameResponse(caseData, bcoPartAdmission);
        } else {
            log.info("CaseId {}: Handling different responses for respondents", caseData.getCcdCaseReference());
            handleDifferentResponses(caseData, bcoPartAdmission);
        }
    }

    private void handleSameResponse(CaseData caseData, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling same response for caseId: {}", caseData.getCcdCaseReference());
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
            log.info("CaseId {}: Respondent responses are the same", caseData.getCcdCaseReference());
            if (bcoPartAdmission.contains(RESPONDENT_1_PAID_LESS)) {
                log.info("Adding tag '{}' for Respondent2 for caseId: {}", RESPONDENT_2_PAID_LESS, caseData.getCcdCaseReference());
                bcoPartAdmission.add(RESPONDENT_2_PAID_LESS);
            }
        } else {
            log.info("CaseId {}: Respondent responses are different", caseData.getCcdCaseReference());
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
            log.info("CaseId {}: Both respondents dispute, updating tags", caseData.getCcdCaseReference());
            bcoPartAdmission.removeAll(bothOnlyDisputes);
            bcoPartAdmission.add(BOTH_RESPONDENTS_DISPUTE);
        }
    }

    private void handleOneVTwoTwoLegalRep(CaseData caseData, Set<DefendantResponseShowTag> tags, Set<DefendantResponseShowTag> bcoPartAdmission) {
        log.info("Handling ONE_V_TWO_TWO_LEGAL_REP scenario for caseId: {}", caseData.getCcdCaseReference());

        if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_1)) {
            log.info("CaseId {}: Handling response for Respondent1", caseData.getCcdCaseReference());
            fullDefenceAndPaidLess(
                    caseData.getRespondent1ClaimResponseTypeForSpec(),
                    caseData.getDefenceRouteRequired(),
                    caseData.getRespondToClaim(),
                    caseData.getTotalClaimAmount(),
                    ONLY_RESPONDENT_1_DISPUTES,
                    DefendantResponseShowTag.RESPONDENT_1_PAID_LESS
            ).ifPresent(bcoPartAdmission::add);
        } else if (tags.contains(DefendantResponseShowTag.CAN_ANSWER_RESPONDENT_2)) {
            log.info("CaseId {}: Handling response for Respondent2", caseData.getCcdCaseReference());
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

    private void populateRespondentResponseTypeSpecPaidStatus(CaseData caseData) {
        log.info("Populating RespondentResponseTypeSpecPaidStatus for caseId: {}", caseData.getCcdCaseReference());
        updateRespondent1PaymentStatus(caseData);

        if (YES.equals(caseData.getIsRespondent2())) {
            log.info("CaseId {}: Respondent2 is present, updating payment status", caseData.getCcdCaseReference());
            updateRespondent2PaymentStatus(caseData);
        }
    }

    private void updateRespondent1PaymentStatus(CaseData caseData) {
        log.info("Updating Respondent1 payment status for caseId: {}", caseData.getCcdCaseReference());

        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired())
                && caseData.getRespondToClaim().getHowMuchWasPaid() != null) {
            int comparison = caseData.getRespondToClaim().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                log.info("CaseId {}: Respondent1 paid less than claimed amount", caseData.getCcdCaseReference());
                caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT);
            } else {
                log.info("CaseId {}: Respondent1 paid full or more than claimed amount", caseData.getCcdCaseReference());
                caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT);
            }
        } else {
            log.info("CaseId {}: Respondent1 did not pay", caseData.getCcdCaseReference());
            caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(RespondentResponseTypeSpecPaidStatus.DID_NOT_PAY);
        }
    }

    private void updateRespondent2PaymentStatus(CaseData caseData) {
        log.info("Updating Respondent2 payment status for caseId: {}", caseData.getCcdCaseReference());

        if (SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED.equals(caseData.getDefenceRouteRequired2())
                && caseData.getRespondToClaim2().getHowMuchWasPaid() != null) {
            int comparison = caseData.getRespondToClaim2().getHowMuchWasPaid()
                    .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(caseData.getTotalClaimAmount())));
            if (comparison < 0) {
                log.info("CaseId {}: Respondent2 paid less than claimed amount", caseData.getCcdCaseReference());
                caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_LESS_THAN_CLAIMED_AMOUNT);
            } else {
                log.info("CaseId {}: Respondent2 paid full or more than claimed amount", caseData.getCcdCaseReference());
                caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(
                        RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT);
            }
        } else {
            log.info("CaseId {}: Respondent2 did not pay", caseData.getCcdCaseReference());
            caseData.setRespondent1ClaimResponsePaymentAdmissionForSpec(null);
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
