package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAppFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    private static final int FREE_GA_DAYS = 14;
    private final RestTemplate restTemplate;
    private final GeneralAppFeesConfiguration feesConfiguration;

    private static final String CHANNEL = "channel";
    private static final String EVENT = "event";
    private static final String JURISDICTION1 = "jurisdiction1";
    private static final String JURISDICTION2 = "jurisdiction2";
    private static final String SERVICE = "service";
    private static final String KEYWORD = "keyword";
    public static final String FREE_REF = "FREE";
    private static final Fee FREE_FEE = Fee.builder()
            .calculatedAmountInPence(BigDecimal.ZERO).code(FREE_REF).version("1").build();

    protected static final List<GeneralApplicationTypes> VARY_TYPES
            = Arrays.asList(GeneralApplicationTypes.VARY_JUDGEMENT,
            GeneralApplicationTypes.VARY_ORDER);
    protected static final List<GeneralApplicationTypes> SET_ASIDE
            = List.of(GeneralApplicationTypes.SET_ASIDE_JUDGEMENT);
    protected static final List<GeneralApplicationTypes> ADJOURN_TYPES
            = List.of(GeneralApplicationTypes.ADJOURN_HEARING);
    protected static final List<GeneralApplicationTypes> SD_CONSENT_TYPES
            = List.of(GeneralApplicationTypes.SETTLE_BY_CONSENT);

    public Fee getFeeForGA(CaseData caseData) {
        Fee result = Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(Integer.MAX_VALUE)).build();
        int typeSize = caseData.getGeneralAppType().getTypes().size();
        if (CollectionUtils.containsAny(caseData.getGeneralAppType().getTypes(), VARY_TYPES)) {
            //only minus 1 as VARY_JUDGEMENT can't be multi selected
            typeSize--;
            result = getFeeForGA(feesConfiguration.getAppnToVaryOrSuspend(), "miscellaneous", "other");
        }
        if (typeSize > 0
                && CollectionUtils.containsAny(caseData.getGeneralAppType().getTypes(), SD_CONSENT_TYPES)) {
            typeSize--;
            Fee sdConsentFeeForGA = getFeeForGA(feesConfiguration.getConsentedOrWithoutNoticeKeyword(), null, null);
            if (sdConsentFeeForGA.getCalculatedAmountInPence()
                    .compareTo(result.getCalculatedAmountInPence()) < 0) {
                result = sdConsentFeeForGA;
            }
        }
        if (typeSize > 0
                && CollectionUtils.containsAny(caseData.getGeneralAppType().getTypes(), SET_ASIDE)) {
            typeSize--;
            Fee setAsideFeeForGA = getFeeForGA(feesConfiguration.getWithNoticeKeyword(), null, null);
            if (setAsideFeeForGA.getCalculatedAmountInPence()
                    .compareTo(result.getCalculatedAmountInPence()) < 0) {
                result = setAsideFeeForGA;
            }
        }
        if (typeSize > 0) {
            Fee defaultFee = getDefaultFee(caseData);
            if (defaultFee.getCalculatedAmountInPence()
                    .compareTo(result.getCalculatedAmountInPence()) < 0) {
                result = defaultFee;
            }
        }
        return result;
    }

    public Fee getFeeForGA(String keyword, String event, String service) {
        if (Objects.isNull(event)) {
            event = feesConfiguration.getEvent();
        }
        if (Objects.isNull(service)) {
            service = feesConfiguration.getService();
        }
        String queryURL = feesConfiguration.getUrl() + feesConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
                .queryParam(CHANNEL, feesConfiguration.getChannel())
                .queryParam(EVENT, event)
                .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
                .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2())
                .queryParam(SERVICE, service)
                .queryParam(KEYWORD, keyword);

        URI uri;
        FeeLookupResponseDto feeLookupResponseDto;
        try {
            uri = builder.buildAndExpand(new HashMap<>()).toUri();
            feeLookupResponseDto = restTemplate.getForObject(uri, FeeLookupResponseDto.class);
        } catch (Exception e) {
            log.error("Fee Service Lookup Failed - " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        if (feeLookupResponseDto == null || feeLookupResponseDto.getFeeAmount() == null) {
            log.error("No Fees returned for [{}].", uri);
            throw new RuntimeException("No Fees returned by fee-service while creating General Application");
        }
        return buildFeeDto(feeLookupResponseDto);
    }

    private Fee getDefaultFee(CaseData caseData) {
        if (isFreeApplication(caseData)) {
            return FREE_FEE;
        } else {
            return getFeeForGA(getFeeRegisterKeyword(caseData), null, null);
        }
    }

    protected String getFeeRegisterKeyword(CaseData caseData) {
        boolean isNotified = caseData.getGeneralAppRespondentAgreement() != null
                && NO.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
                && caseData.getGeneralAppInformOtherParty() != null
                && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
        return isNotified
                ? feesConfiguration.getWithNoticeKeyword()
                : feesConfiguration.getConsentedOrWithoutNoticeKeyword();
    }

    public boolean isFreeApplication(final CaseData caseData) {
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

        return Fee.builder()
                .calculatedAmountInPence(calculatedAmount)
                .code(feeLookupResponseDto.getCode())
                .version(feeLookupResponseDto.getVersion().toString())
                .build();
    }
}
