package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAppFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    private static final int FREE_GA_DAYS = 14;

    private final FeesApiClient feesApiClient;
    private final GeneralAppFeesConfiguration feesConfiguration;
    public static final String FREE_REF = "FREE";
    private static final Fee FREE_FEE = Fee.builder()
        .calculatedAmountInPence(BigDecimal.ZERO).code(FREE_REF).version("1").build();
    private static final String MISCELLANEOUS = "miscellaneous";
    private static final String OTHER = "other";

    protected static final List<GeneralApplicationTypes> VARY_TYPES
        = Arrays.asList(
        GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT
    );
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

    public Fee getFeeForGA(CaseData caseData) {
        return getFeeForGA(
            caseData.getGeneralAppType().getTypes(),
            getRespondentAgreed(caseData),
            getInformOtherParty(caseData),
            getHearingDate(caseData)
        );
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
        Fee result = Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(Integer.MAX_VALUE)).build();
        int typeSize = types.size();
        if (CollectionUtils.containsAny(types, VARY_TYPES)) {
            //only minus 1 as VARY_PAYMENT_TERMS_OF_JUDGMENT can't be multi selected
            typeSize--;
            result = getFeeForGA(feesConfiguration.getAppnToVaryOrSuspend(), MISCELLANEOUS, OTHER);
        }
        if (typeSize > 0
            && CollectionUtils.containsAny(types, SD_CONSENT_TYPES)) {
            typeSize--;
            Fee sdConsentFeeForGA = getFeeForGA(feesConfiguration.getConsentedOrWithoutNoticeKeyword(), null, null);
            if (sdConsentFeeForGA.getCalculatedAmountInPence()
                .compareTo(result.getCalculatedAmountInPence()) < 0) {
                result = sdConsentFeeForGA;
            }
        }
        if (isUpdateCoScGATypeSize(typeSize, types)) {
            typeSize--;
            Fee certOfSatisfactionOrCancel = getFeeForGA(feesConfiguration.getCertificateOfSatisfaction(), "miscellaneous", "other");
            result = getCoScFeeResult(result, certOfSatisfactionOrCancel);
        }
        if (typeSize > 0) {
            Fee defaultFee = getDefaultFee(types, respondentAgreed, informOtherParty, hearingScheduledDate);
            if (defaultFee.getCalculatedAmountInPence()
                .compareTo(result.getCalculatedAmountInPence()) < 0) {
                result = defaultFee;
            }
        }
        return result;
    }

    protected Fee getFeeForGA(String keyword, String event, String service) {
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
            log.error("Fee Service Lookup Failed - " + e.getMessage(), e);
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

    protected String getFeeRegisterKeyword(Boolean respondentAgreed, Boolean informOtherParty) {
        boolean isNotified = respondentAgreed != null
            && !respondentAgreed
            && informOtherParty != null
            && informOtherParty;
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

    private Fee buildFeeDto(FeeLookupResponseDto feeLookupResponseDto) {
        BigDecimal calculatedAmount = feeLookupResponseDto.getFeeAmount()
            .multiply(PENCE_PER_POUND)
            .setScale(0, RoundingMode.UNNECESSARY);

        return Fee.builder()
            .calculatedAmountInPence(calculatedAmount)
            .code(feeLookupResponseDto.getCode())
            .version(feeLookupResponseDto.getVersion().toString())
            .build();
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

    private boolean isUpdateCoScGATypeSize(int typeSize, List<GeneralApplicationTypes> types) {
        return typeSize > 0 && CollectionUtils.containsAny(types, CONFIRM_YOU_PAID_CCJ_DEBT);
    }

    private Fee getCoScFeeResult(Fee existingResult, Fee certOfSatisfactionOrCancel) {
        if (certOfSatisfactionOrCancel.getCalculatedAmountInPence().compareTo(existingResult.getCalculatedAmountInPence()) < 0) {
            return certOfSatisfactionOrCancel;
        }
        return existingResult;
    }
}
