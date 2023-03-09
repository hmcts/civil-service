package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import java.util.HashMap;

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

    public Fee getFeeForGA(CaseData caseData) {
        String queryURL = feesConfiguration.getUrl() + feesConfiguration.getEndpoint();
        String keyword = getKeyword(caseData);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
                .queryParam(CHANNEL, feesConfiguration.getChannel())
                .queryParam(EVENT, feesConfiguration.getEvent())
                .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
                .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2())
                .queryParam(SERVICE, feesConfiguration.getService())
                .queryParam(KEYWORD, keyword);
        //TODO remove this if block after we have real free fee for GA
        if (feesConfiguration.getFreeKeyword().equals(keyword)) {
            builder = UriComponentsBuilder.fromUriString(queryURL)
                    .queryParam(CHANNEL, feesConfiguration.getChannel())
                    .queryParam(EVENT, "copies")
                    .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
                    .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2())
                    .queryParam(SERVICE, "insolvency")
                    .queryParam(KEYWORD, feesConfiguration.getFreeKeyword());
        }
        if (feesConfiguration.getAppnToVaryOrSuspend().equals(keyword)) {
            builder = UriComponentsBuilder.fromUriString(queryURL)
                .queryParam(CHANNEL, feesConfiguration.getChannel())
                .queryParam(EVENT, "miscellaneous")
                .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
                .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2())
                .queryParam(SERVICE, "other")
                .queryParam(KEYWORD, feesConfiguration.getAppnToVaryOrSuspend());
        }
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

    private boolean isFreeApplication(final CaseData caseData) {
        if (caseData.getGeneralAppType().getTypes().size() == 1
            && caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.ADJOURN_VACATE_HEARING)
            && caseData.getGeneralAppRespondentAgreement() != null
            && YES.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
            && caseData.getGeneralAppHearingDate() != null
            && caseData.getGeneralAppHearingDate().getHearingScheduledDate() != null) {
            return caseData.getGeneralAppHearingDate().getHearingScheduledDate()
                    .isAfter(LocalDate.now().plusDays(FREE_GA_DAYS));
        }
        return false;
    }

    private boolean isOnlyVaryOrSuspendApplication(CaseData caseData) {
        if (caseData.getGeneralAppType().getTypes().size() == 1) {
            return caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_JUDGEMENT)
                ? true
                : caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_ORDER) ? true : false;
        }
        return false;
    }

    private boolean hasAppContainVaryOrder(CaseData caseData) {
        return caseData.getGeneralAppType().getTypes().contains(GeneralApplicationTypes.VARY_ORDER);
    }

    private String getKeyword(CaseData caseData) {
        if (isFreeApplication(caseData)) {
            return feesConfiguration.getFreeKeyword();
        }

        if (isOnlyVaryOrSuspendApplication(caseData)) {
            return feesConfiguration.getAppnToVaryOrSuspend();
        }

        if (hasAppContainVaryOrder(caseData)) {
            //TODO:- CIV-7575 is been created to handle application to Vary Order fee when multiple application types
            return feesConfiguration.getAppnToVaryOrSuspend();
        }

        boolean isNotified = caseData.getGeneralAppRespondentAgreement() != null
                && NO.equals(caseData.getGeneralAppRespondentAgreement().getHasAgreed())
                && caseData.getGeneralAppInformOtherParty() != null
                && YES.equals(caseData.getGeneralAppInformOtherParty().getIsWithNotice());
        return isNotified
                ? feesConfiguration.getWithNoticeKeyword()
                : feesConfiguration.getConsentedOrWithoutNoticeKeyword();
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
