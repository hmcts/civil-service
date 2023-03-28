package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.HashMap;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    private final RestTemplate restTemplate;
    private final HearingFeeConfiguration feesConfiguration;

    private static final String CHANNEL = "channel";
    private static final String EVENT = "event";
    private static final String JURISDICTION1 = "jurisdiction1";
    private static final String JURISDICTION2 = "jurisdiction2";
    private static final String SERVICE = "service";
    private static final String KEYWORD = "keyword";
    private static final String AMOUNT = "amount_or_volume";

    public Fee getFeeForHearingSmallClaims(BigDecimal amount) {
        String queryURL = feesConfiguration.getUrl() + feesConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam(CHANNEL, feesConfiguration.getChannel())
            .queryParam(EVENT, feesConfiguration.getHearingEvent())
            .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
            .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2())
            .queryParam(SERVICE, feesConfiguration.getService())
            .queryParam(AMOUNT, amount);

        return getRespond(builder);
    }

    public Fee getFeeForHearingFastTrackClaims(BigDecimal amount) {
        String queryURL = feesConfiguration.getUrl() + feesConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam(CHANNEL, feesConfiguration.getChannel())
            .queryParam(EVENT, feesConfiguration.getHearingEvent())
            .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
            .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2Hearing())
            .queryParam(SERVICE, feesConfiguration.getService())
            .queryParam(KEYWORD, feesConfiguration.getFastTrackHrgKey())
            .queryParam(AMOUNT, amount);

        return getRespond(builder);
    }

    public Fee getFeeForHearingMultiClaims(BigDecimal amount) {
        String queryURL = feesConfiguration.getUrl() + feesConfiguration.getEndpoint();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(queryURL)
            .queryParam(CHANNEL, feesConfiguration.getChannel())
            .queryParam(EVENT, feesConfiguration.getHearingEvent())
            .queryParam(JURISDICTION1, feesConfiguration.getJurisdiction1())
            .queryParam(JURISDICTION2, feesConfiguration.getJurisdiction2Hearing())
            .queryParam(SERVICE, feesConfiguration.getService())
            .queryParam(KEYWORD, feesConfiguration.getMultiClaimKey())
            .queryParam(AMOUNT, amount);

        return getRespond(builder);
    }

    private Fee getRespond(UriComponentsBuilder builder) {
        FeeLookupResponseDto feeLookupResponseDto;
        URI uri;
        try {
            uri = builder.buildAndExpand(new HashMap<>()).toUri();
            feeLookupResponseDto = restTemplate.getForObject(uri, FeeLookupResponseDto.class);
        } catch (Exception e) {
            log.error("Fee Service Lookup Failed - " + e.getMessage(), e);
            throw new RestClientException("Fee Service Lookup Failed");
        }
        if (isNull(feeLookupResponseDto) || isNull(feeLookupResponseDto.getFeeAmount())) {
            log.error("No Fees returned for [{}].", uri);
            throw new RestClientException("No Fees returned by fee-service while creating hearing fee");
        }
        return buildFeeDto(feeLookupResponseDto);
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
