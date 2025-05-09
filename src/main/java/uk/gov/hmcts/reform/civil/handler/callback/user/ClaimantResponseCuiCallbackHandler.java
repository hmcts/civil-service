package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.UnavailabilityDatesUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIMANT_RESPONSE_CUI;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ExpertUtils.addEventAndDateAddedToApplicantExperts;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.populateDQPartyIds;
import static uk.gov.hmcts.reform.civil.utils.WitnessUtils.addEventAndDateAddedToApplicantWitnesses;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantResponseCuiCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE_CUI);

    private final ResponseOneVOneShowTagService responseOneVOneService;
    private final FeatureToggleService featureToggleService;
    private final JudgementService judgementService;

    private final ObjectMapper objectMapper;
    private final Time time;
    private final UpdateCaseManagementDetailsService updateCaseManagementLocationDetailsService;
    private final DeadlinesCalculator deadlinesCalculator;
    private final CaseFlagsInitialiser caseFlagsInitialiser;
    private final JudgmentByAdmissionOnlineMapper judgmentByAdmissionOnlineMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateCaseData,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateCaseData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedCaseData = caseData.toBuilder();
        updatedCaseData.showResponseOneVOneFlag(responseOneVOneService.setUpOneVOneFlow(caseData));

        Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
            .map(RespondToClaim::getHowMuchWasPaid);

        howMuchWasPaid.ifPresent(howMuchWasPaidValue -> updatedCaseData.partAdmitPaidValuePounds(
            MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime applicant1ResponseDate = LocalDateTime.now();

        CaseData.CaseDataBuilder<?, ?> builder = caseData.toBuilder()
            .applicant1ResponseDate(applicant1ResponseDate)
            .businessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI))
            .respondent1RespondToSettlementAgreementDeadline(caseData.isClaimantBilingual() ? null : getRespondToSettlementAgreementDeadline(
                caseData,
                applicant1ResponseDate
            ))
            .nextDeadline(null);

        updateCaseManagementLocationDetailsService.updateCaseManagementDetails(builder, callbackParams);

        if (caseData.hasClaimantAgreedToFreeMediation() && caseData.hasDefendantAgreedToFreeMediation()
            || (featureToggleService.isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && (YES.equals(caseData.getApplicant1ProceedWithClaim())
            || NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim())))) {
            builder.claimMovedToMediationOn(LocalDate.now());
        }
        updateCcjRequestPaymentDetails(builder, caseData);
        populateDQPartyIds(builder);
        addEventAndDateAddedToApplicantExperts(builder);
        addEventAndDateAddedToApplicantWitnesses(builder);

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE_CUI, builder);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(builder);

        if (featureToggleService.isJudgmentOnlineLive() && JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData)) {
            CaseData updatedCaseData = builder.build();
            builder
                .activeJudgment(judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(updatedCaseData))
                .joIsLiveJudgmentExists(YES)
                .joJudgementByAdmissionIssueDate(LocalDateTime.now());
        }

        CaseData updatedData = builder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper));

        return response.build();
    }

    private LocalDateTime getRespondToSettlementAgreementDeadline(CaseData caseData, LocalDateTime responseDate) {
        if (caseData.hasApplicant1SignedSettlementAgreement()) {
            return caseData.isCourtDecisionInClaimantFavourImmediateRePayment()
                    ? deadlinesCalculator.getRespondentToImmediateSettlementAgreement(responseDate)
                    : deadlinesCalculator.getRespondToSettlementAgreementDeadline(responseDate);
        }
        return null;
    }

    private void updateCcjRequestPaymentDetails(CaseData.CaseDataBuilder<?, ?> builder, CaseData caseData) {
        if (hasCcjRequest(caseData)) {
            CCJPaymentDetails ccjPaymentDetails = judgementService.buildJudgmentAmountSummaryDetails(caseData);
            builder.ccjPaymentDetails(ccjPaymentDetails).build();
        }
    }

    private boolean hasCcjRequest(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
            && caseData.hasApplicant1AcceptedCcj() && caseData.isCcjRequestJudgmentByAdmission());
    }
}
