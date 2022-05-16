package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.civil.config.GeneralAppFeesConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.HashMap;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneralAppFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);

    private final RestTemplate restTemplate;
    private final GeneralAppFeesConfiguration feesConfiguration;

    public Fee getFeeForGA(CaseData caseData) {
        String queryURL = feesConfiguration.getUrl() + feesConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
                .queryParam("channel", feesConfiguration.getChannel())
                .queryParam("event", feesConfiguration.getEvent())
                .queryParam("jurisdiction1", feesConfiguration.getJurisdiction1())
                .queryParam("jurisdiction2", feesConfiguration.getJurisdiction2())
                .queryParam("service", feesConfiguration.getService())
                .queryParam("keyword", getKeyword(caseData));
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

    private String getKeyword(CaseData caseData) {
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
