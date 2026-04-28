package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAppFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    protected static final int FREE_GA_DAYS = 14;

    private final FeesApiClient feesApiClient;
    private final GeneralAppFeesConfiguration feesConfiguration;
    public static final String FREE_REF = "FREE";
    private static final Fee FREE_FEE;

    static {
        FREE_FEE = new Fee();
        FREE_FEE.setCalculatedAmountInPence(BigDecimal.ZERO);
        FREE_FEE.setCode(FREE_REF);
        FREE_FEE.setVersion("1");
    }

    private static final String MISCELLANEOUS = "miscellaneous";
    private static final String OTHER = "other";

    protected static final List<GeneralApplicationTypes> VARY_TYPES
        = List.of(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
    protected static final List<GeneralApplicationTypes> SET_ASIDE
        = List.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT);
    protected static final List<GeneralApplicationTypes> ADJOURN_TYPES
        = List.of(GeneralApplicationTypes.ADJOURN_HEARING);
    protected static final List<GeneralApplicationTypes> SD_CONSENT_TYPES
        = List.of(GeneralApplicationTypes.SETTLE_BY_CONSENT);
    protected static final List<GeneralApplicationTypes> CONFIRM_YOU_PAID_CCJ_DEBT
        = List.of(GeneralApplicationTypes.CONFIRM_CCJ_DEBT_PAID);

    public Fee getFeeForJOWithApplicationType(GeneralApplicationTypes applicationType) {
        return switch (applicationType) {
            case VARY_ORDER -> getFeeForGA(feesConfiguration.getAppnToVaryOrSuspend(), MISCELLANEOUS, OTHER);
            case SET_ASIDE_JUDGEMENT -> getFeeForGA(feesConfiguration.getWithNoticeKeyword(), "general application", "general");
            case OTHER -> getFeeForGA(feesConfiguration.getCertificateOfSatisfaction(), MISCELLANEOUS, OTHER);
            default -> null;
        };
    }

    public Fee getFeeForGALiP(List<GeneralApplicationTypes> applicationTypes, Boolean withConsent,
                              Boolean withNotice, LocalDate hearingDate) {
        return getFeeForGA(applicationTypes, withConsent, withNotice, hearingDate);
    }

    public boolean isFreeGa(GeneralApplication application) {
        if (application.getGeneralAppType().getTypes().size() == 1
            && application.getGeneralAppType().getTypes()
            .contains(GeneralApplicationTypes.ADJOURN_HEARING)
            && application.getGeneralAppRespondentAgreement() != null
            && YES.equals(application.getGeneralAppRespondentAgreement().getHasAgreed())
            && application.getGeneralAppHearingDate() != null
            && application.getGeneralAppHearingDate().getHearingScheduledDate() != null) {
            return application.getGeneralAppHearingDate().getHearingScheduledDate()
                .isAfter(LocalDate.now().plusDays(GeneralAppFeesService.FREE_GA_DAYS));
        }
        return false;
    }

    public Fee getFeeForGA(CaseData caseData) {
        return getFeeForGA(
            caseData.getGeneralAppType().getTypes(),
            getRespondentAgreed(caseData),
            getInformOtherParty(caseData),
            getHearingDate(caseData)
        );
    }

    public Fee getFeeForGA(GeneralApplicationCaseData caseData) {
        List<GeneralApplicationTypes> types = caseData.getGeneralAppType().getTypes();
        FeeCalculationState calculationState = initialCalculationState(types);
        calculationState = applyVaryFee(calculationState, types);
        calculationState = applySettlementByConsentFee(calculationState, types);
        calculationState = applyCaseDataSetAsideFee(calculationState, caseData, types);
        calculationState = applyCertificateOfSatisfactionFee(calculationState, types);
        return applyDefaultCaseDataFee(calculationState, caseData);
    }

    public Fee getFeeForGA(GeneralApplication generalApplication, LocalDate hearingScheduledDate) {
        return getFeeForGA(
            generalApplication.getGeneralAppType().getTypes(),
            getRespondentAgreed(generalApplication),
            getInformOtherParty(generalApplication),
            hearingScheduledDate
        );
    }

    private Fee getFeeForGA(List<GeneralApplicationTypes> types, Boolean respondentAgreed, Boolean informOtherParty, LocalDate hearingScheduledDate) {
        FeeCalculationState calculationState = initialCalculationState(types);
        calculationState = applyVaryFee(calculationState, types);
        calculationState = applySettlementByConsentFee(calculationState, types);
        calculationState = applyListSetAsideFee(calculationState, types, respondentAgreed, informOtherParty);
        calculationState = applyCertificateOfSatisfactionFee(calculationState, types);
        return applyDefaultListFee(calculationState, types, respondentAgreed, informOtherParty, hearingScheduledDate);
    }

    public Fee getFeeForGA(String keyword, String event, String service) {
        if (Objects.isNull(event)) {
            event = feesConfiguration.getEvent();
        }
        if (Objects.isNull(service)) {
            service = feesConfiguration.getService();
        }

        FeeLookupResponseDto feeLookupResponseDto;
        try {
            feeLookupResponseDto = feesApiClient.lookupFee(
                service,
                feesConfiguration.getJurisdiction1(),
                feesConfiguration.getJurisdiction2(),
                feesConfiguration.getChannel(),
                event,
                keyword
            );
            log.info(
                "Received fee service response, amount: {}",
                Optional.ofNullable(feeLookupResponseDto).map(FeeLookupResponseDto::getFeeAmount).orElse(null)
            );
        } catch (Exception e) {
            log.error("Fee Service Lookup Failed - {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        if (feeLookupResponseDto == null || feeLookupResponseDto.getFeeAmount() == null) {
            throw new RuntimeException("No Fees returned by fee-service while creating General Application");
        }
        return buildFeeDto(feeLookupResponseDto);
    }

    private Fee getDefaultFee(List<GeneralApplicationTypes> types, Boolean respondentAgreed, Boolean informOtherParty, LocalDate hearingScheduledDate) {
        if (isFreeApplication(types, respondentAgreed, hearingScheduledDate)) {
            return FREE_FEE;
        } else {
            return getFeeForGA(getFeeRegisterKeyword(respondentAgreed, informOtherParty), null, null);
        }
    }

    private Fee getDefaultFee(GeneralApplicationCaseData caseData) {
        if (isFreeApplication(caseData)) {
            return FREE_FEE;
        } else {
            return getFeeForGA(getFeeRegisterKeyword(caseData), null, null);
        }
    }

    protected String getFeeRegisterKeyword(Boolean respondentAgreed, Boolean informOtherParty) {
        boolean isNotified = respondentAgreed != null
            && !respondentAgreed
            && informOtherParty != null
            && informOtherParty;
        return isNotified
            ? feesConfiguration.getWithNoticeKeyword()
            : feesConfiguration.getConsentedOrWithoutNoticeKeyword();
    }

    protected String getFeeRegisterKeyword(GeneralApplicationCaseData caseData) {
        boolean isNotified = caseData.getGeneralAppRespondentAgreement() != null
            && NO.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
            && caseData.getGeneralAppInformOtherParty() != null
            && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
        return isNotified
            ? feesConfiguration.getWithNoticeKeyword()
            : feesConfiguration.getConsentedOrWithoutNoticeKeyword();
    }

    protected boolean isFreeApplication(List<GeneralApplicationTypes> types, Boolean respondentAgreed, LocalDate hearingScheduledDate) {
        return types.size() == 1
            && types.contains(GeneralApplicationTypes.ADJOURN_HEARING)
            && Boolean.TRUE.equals(respondentAgreed)
            && hearingScheduledDate != null
            && hearingScheduledDate.isAfter(LocalDate.now().plusDays(FREE_GA_DAYS));
    }

    public boolean isFreeApplication(final GeneralApplicationCaseData caseData) {
        if (caseData.getGeneralAppType().getTypes().size() == 1
            && caseData.getGeneralAppType().getTypes()
            .contains(GeneralApplicationTypes.ADJOURN_HEARING)
            && caseData.getGeneralAppRespondentAgreement() != null
            && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
            && caseData.getGeneralAppHearingDate() != null
            && caseData.getGeneralAppHearingDate().getHearingScheduledDate() != null) {
            return caseData.getGeneralAppHearingDate().getHearingScheduledDate()
                .isAfter(LocalDate.now().plusDays(FREE_GA_DAYS));
        }
        return false;
    }

    private Fee buildFeeDto(FeeLookupResponseDto feeLookupResponseDto) {
        BigDecimal calculatedAmount = feeLookupResponseDto.getFeeAmount()
            .multiply(PENCE_PER_POUND)
            .setScale(0, RoundingMode.UNNECESSARY);

        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(calculatedAmount);
        fee.setCode(feeLookupResponseDto.getCode());
        fee.setVersion(feeLookupResponseDto.getVersion().toString());
        return fee;
    }

    protected Boolean getRespondentAgreed(CaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppRespondentAgreement())
            .map(GARespondentOrderAgreement::getHasAgreed)
            .map(hasAgreed -> hasAgreed == YES)
            .orElse(null);
    }

    protected Boolean getRespondentAgreed(GeneralApplication generalApplication) {
        return Optional.ofNullable(generalApplication.getGeneralAppRespondentAgreement())
            .map(GARespondentOrderAgreement::getHasAgreed)
            .map(hasAgreed -> hasAgreed == YES)
            .orElse(null);
    }

    protected Boolean getInformOtherParty(CaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppInformOtherParty())
            .map(GAInformOtherParty::getIsWithNotice)
            .map(isWithNotice -> isWithNotice == YES)
            .orElse(null);
    }

    protected Boolean getInformOtherParty(GeneralApplication generalApplication) {
        return Optional.ofNullable(generalApplication.getGeneralAppInformOtherParty())
            .map(GAInformOtherParty::getIsWithNotice)
            .map(isWithNotice -> isWithNotice == YES)
            .orElse(null);
    }

    protected LocalDate getHearingDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppHearingDate())
            .map(GAHearingDateGAspec::getHearingScheduledDate)
            .orElse(null);
    }

    protected LocalDate getHearingDate(GeneralApplicationCaseData caseData) {
        return Optional.ofNullable(caseData.getGeneralAppHearingDate())
            .map(GAHearingDateGAspec::getHearingScheduledDate)
            .orElse(null);
    }

    private boolean isUpdateCoScGATypeSize(int typeSize, List<GeneralApplicationTypes> types) {
        return typeSize > 0 && CollectionUtils.containsAny(types, CONFIRM_YOU_PAID_CCJ_DEBT);
    }

    private Fee getCoScFeeResult(Fee existingResult, Fee certOfSatisfactionOrCancel) {
        if (certOfSatisfactionOrCancel.getCalculatedAmountInPence().compareTo(existingResult.getCalculatedAmountInPence()) < 0) {
            return certOfSatisfactionOrCancel;
        }
        return existingResult;
    }

    private FeeCalculationState initialCalculationState(List<GeneralApplicationTypes> types) {
        return new FeeCalculationState(createMaxFee(), types.size());
    }

    private FeeCalculationState applyVaryFee(FeeCalculationState calculationState, List<GeneralApplicationTypes> types) {
        if (!CollectionUtils.containsAny(types, VARY_TYPES)) {
            return calculationState;
        }
        // only minus 1 as VARY_PAYMENT_TERMS_OF_JUDGMENT can't be multi selected
        return calculationState.withFeeAndRemainingTypes(
            getFeeForGA(feesConfiguration.getAppnToVaryOrSuspend(), MISCELLANEOUS, OTHER),
            calculationState.remainingTypes() - 1
        );
    }

    private FeeCalculationState applySettlementByConsentFee(FeeCalculationState calculationState,
                                                            List<GeneralApplicationTypes> types) {
        if (calculationState.remainingTypes() <= 0 || !CollectionUtils.containsAny(types, SD_CONSENT_TYPES)) {
            return calculationState;
        }
        return applyCandidateFee(
            calculationState,
            getFeeForGA(feesConfiguration.getConsentedOrWithoutNoticeKeyword(), null, null)
        );
    }

    private FeeCalculationState applyCaseDataSetAsideFee(FeeCalculationState calculationState,
                                                         GeneralApplicationCaseData caseData,
                                                         List<GeneralApplicationTypes> types) {
        if (!shouldApplyCaseDataSetAsideFee(calculationState.remainingTypes(), caseData, types)) {
            return calculationState;
        }
        return applyCandidateFee(
            calculationState,
            getFeeForGA(getSetAsideKeyword(caseData), null, null)
        );
    }

    private boolean shouldApplyCaseDataSetAsideFee(int remainingTypes,
                                                   GeneralApplicationCaseData caseData,
                                                   List<GeneralApplicationTypes> types) {
        return remainingTypes > 0
            && CollectionUtils.containsAny(types, SET_ASIDE)
            && caseData.getGeneralAppRespondentAgreement() != null
            && NO.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed());
    }

    private String getSetAsideKeyword(GeneralApplicationCaseData caseData) {
        return caseData.getGeneralAppInformOtherParty() != null
            && NO.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice())
            ? feesConfiguration.getConsentedOrWithoutNoticeKeyword()
            : feesConfiguration.getWithNoticeKeyword();
    }

    private FeeCalculationState applyListSetAsideFee(FeeCalculationState calculationState,
                                                     List<GeneralApplicationTypes> types,
                                                     Boolean respondentAgreed,
                                                     Boolean informOtherParty) {
        if (!shouldApplyListSetAsideFee(calculationState.remainingTypes(), types, respondentAgreed)) {
            return calculationState;
        }
        String feeKeyword = Boolean.FALSE.equals(informOtherParty)
            ? feesConfiguration.getConsentedOrWithoutNoticeKeyword()
            : feesConfiguration.getWithNoticeKeyword();
        return applyCandidateFee(calculationState, getFeeForGA(feeKeyword, null, null));
    }

    private boolean shouldApplyListSetAsideFee(int remainingTypes,
                                               List<GeneralApplicationTypes> types,
                                               Boolean respondentAgreed) {
        return remainingTypes > 0
            && CollectionUtils.containsAny(types, SET_ASIDE)
            && Boolean.FALSE.equals(respondentAgreed);
    }

    private FeeCalculationState applyCertificateOfSatisfactionFee(FeeCalculationState calculationState,
                                                                  List<GeneralApplicationTypes> types) {
        if (!isUpdateCoScGATypeSize(calculationState.remainingTypes(), types)) {
            return calculationState;
        }
        return applyCandidateFee(
            calculationState,
            getFeeForGA(feesConfiguration.getCertificateOfSatisfaction(), MISCELLANEOUS, OTHER)
        );
    }

    private Fee applyDefaultCaseDataFee(FeeCalculationState calculationState, GeneralApplicationCaseData caseData) {
        return applyDefaultFee(calculationState, () -> getDefaultFee(caseData));
    }

    private Fee applyDefaultListFee(FeeCalculationState calculationState,
                                    List<GeneralApplicationTypes> types,
                                    Boolean respondentAgreed,
                                    Boolean informOtherParty,
                                    LocalDate hearingScheduledDate) {
        return applyDefaultFee(
            calculationState,
            () -> getDefaultFee(types, respondentAgreed, informOtherParty, hearingScheduledDate)
        );
    }

    private Fee applyDefaultFee(FeeCalculationState calculationState, Supplier<Fee> defaultFeeSupplier) {
        if (calculationState.remainingTypes() <= 0) {
            return calculationState.fee();
        }
        return getCoScFeeResult(calculationState.fee(), defaultFeeSupplier.get());
    }

    private FeeCalculationState applyCandidateFee(FeeCalculationState calculationState, Fee candidateFee) {
        return calculationState.withFeeAndRemainingTypes(
            getCoScFeeResult(calculationState.fee(), candidateFee),
            calculationState.remainingTypes() - 1
        );
    }

    private Fee createMaxFee() {
        Fee fee = new Fee();
        fee.setCalculatedAmountInPence(BigDecimal.valueOf(Integer.MAX_VALUE));
        return fee;
    }

    private record FeeCalculationState(Fee fee, int remainingTypes) {
        private FeeCalculationState withFeeAndRemainingTypes(Fee updatedFee, int updatedRemainingTypes) {
            return new FeeCalculationState(updatedFee, updatedRemainingTypes);
        }
    }
}
