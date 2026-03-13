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
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.JudgementService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.PaymentDateService;
import uk.gov.hmcts.reform.civil.service.citizenui.ResponseOneVOneShowTagService;
import uk.gov.hmcts.reform.civil.service.citizen.UpdateCaseManagementDetailsService;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentByAdmissionOnlineMapper;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.JudgmentAdmissionUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;
import uk.gov.hmcts.reform.civil.utils.RequestedCourtForClaimDetailsTab;
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
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.PaymentDateService.DATE_FORMATTER;
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
    private final RequestedCourtForClaimDetailsTab requestedCourtForClaimDetailsTab;
    private final PaymentDateService paymentDateService;

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
        caseData.setShowResponseOneVOneFlag(responseOneVOneService.setUpOneVOneFlow(caseData));

        Optional<BigDecimal> howMuchWasPaid = Optional.ofNullable(caseData.getRespondToAdmittedClaim())
            .map(RespondToClaim::getHowMuchWasPaid);

        howMuchWasPaid.ifPresent(howMuchWasPaidValue -> caseData.setPartAdmitPaidValuePounds(
            MonetaryConversions.penniesToPounds(howMuchWasPaidValue)));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private boolean isDefendantPartAdmitPayImmediatelyAccepted(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && IMMEDIATELY.equals(caseData.getDefenceAdmitPartPaymentTimeRouteRequired())
            && (PART_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()))
            && caseData.hasApplicantAcceptedRepaymentPlan();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime applicant1ResponseDate = LocalDateTime.now();

        caseData.setApplicant1ResponseDate(applicant1ResponseDate);
        caseData.setBusinessProcess(BusinessProcess.ready(CLAIMANT_RESPONSE_CUI));
        caseData.setRespondent1RespondToSettlementAgreementDeadline(caseData.isClaimantBilingual()
                                                                 || caseData.isRespondentResponseBilingual()
                                                                 ? null
                                                                 : getRespondToSettlementAgreementDeadline(caseData, applicant1ResponseDate));
        caseData.setNextDeadline(null);

        updateCaseManagementLocationDetailsService.updateCaseManagementDetails(caseData, callbackParams);

        if (caseData.hasClaimantAgreedToFreeMediation() && caseData.hasDefendantAgreedToFreeMediation()
            || (featureToggleService.isCarmEnabledForCase(caseData)
            && SMALL_CLAIM.name().equals(caseData.getResponseClaimTrack())
            && (YES.equals(caseData.getApplicant1ProceedWithClaim())
            || NO.equals(caseData.getCaseDataLiP().getApplicant1SettleClaim())))) {
            caseData.setClaimMovedToMediationOn(LocalDate.now());
        }

        if (isDefendantPartAdmitPayImmediatelyAccepted(caseData)) {
            LocalDate whenBePaid = paymentDateService.calculatePaymentDeadline();
            caseData.setWhenToBePaidText(whenBePaid.format(DATE_FORMATTER));
            caseData.setRespondToClaimAdmitPartLRspec(new RespondToClaimAdmitPartLRspec()
                                                      .setWhenWillThisAmountBePaid(whenBePaid));
        }

        updateCcjRequestPaymentDetails(caseData);
        updateLanguagePreference(caseData);

        populateDQPartyIds(caseData);
        addEventAndDateAddedToApplicantExperts(caseData);
        addEventAndDateAddedToApplicantWitnesses(caseData);

        caseFlagsInitialiser.initialiseCaseFlags(CLAIMANT_RESPONSE_CUI, caseData);

        UnavailabilityDatesUtils.rollUpUnavailabilityDatesForApplicant(caseData);

        requestedCourtForClaimDetailsTab.updateRequestCourtClaimTabApplicant(callbackParams, caseData);

        if (featureToggleService.isJudgmentOnlineLive() && JudgmentAdmissionUtils.getLIPJudgmentAdmission(caseData)) {
            JudgmentDetails activeJudgmentDetails = judgmentByAdmissionOnlineMapper.addUpdateActiveJudgment(caseData);
            caseData.setActiveJudgment(activeJudgmentDetails);
            caseData.setJoIsLiveJudgmentExists(YES);
            caseData.setJoJudgementByAdmissionIssueDate(time.now());
            caseData.setJoRepaymentSummaryObject(JudgmentsOnlineHelper.calculateRepaymentBreakdownSummaryWithoutClaimInterest(
                activeJudgmentDetails,
                true
            ));
        }

        if ((AllocatedTrack.MULTI_CLAIM.name().equals(caseData.getResponseClaimTrack())
            || AllocatedTrack.INTERMEDIATE_CLAIM.name().equals(caseData.getResponseClaimTrack()))
            && featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            caseData.setIsMintiLipCase(YES);
        }

        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper));

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

    private void updateCcjRequestPaymentDetails(CaseData caseData) {
        if (hasCcjRequest(caseData)) {
            CCJPaymentDetails ccjPaymentDetails = judgementService.buildJudgmentAmountSummaryDetails(caseData);
            caseData.setCcjPaymentDetails(ccjPaymentDetails);
        }
    }

    private void updateLanguagePreference(CaseData caseData) {
        if (featureToggleService.isWelshEnabledForMainCase()) {
            Optional.ofNullable(caseData.getApplicant1DQ())
                .map(Applicant1DQ::getApplicant1DQLanguage).map(WelshLanguageRequirements::getDocuments)
                .ifPresent(documentLanguage -> caseData.setClaimantBilingualLanguagePreference(documentLanguage.name()));
        }
    }

    private boolean hasCcjRequest(CaseData caseData) {
        return (caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled()
            && caseData.hasApplicant1AcceptedCcj() && caseData.isCcjRequestJudgmentByAdmission());
    }
}
