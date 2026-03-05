package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.civil.client.FeesApiClient;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.civil.model.FeesApiRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    private final FeesApiClient feesApiClient;
    private final HearingFeeConfiguration feesConfiguration;

    public Fee getFeeForHearingSmallClaims(BigDecimal amount) {
        FeesApiRequest builder = new FeesApiRequest()
            .setChannel(feesConfiguration.getChannel())
            .setEvent(feesConfiguration.getHearingEvent())
            .setJurisdiction(feesConfiguration.getJurisdiction1())
            .setJurisdiction2(feesConfiguration.getJurisdiction2())
            .setService(feesConfiguration.getService())
            .setKeyword(feesConfiguration.getSmallClaimHrgKey())
            .setAmount(amount);

        return getRespond(builder);
    }

    public Fee getFeeForHearingFastTrackClaims(BigDecimal amount) {

        FeesApiRequest builder = new FeesApiRequest()
            .setChannel(feesConfiguration.getChannel())
            .setEvent(feesConfiguration.getHearingEvent())
            .setJurisdiction(feesConfiguration.getJurisdiction1())
            .setJurisdiction2(feesConfiguration.getJurisdiction2Hearing())
            .setService(feesConfiguration.getService())
            .setKeyword(feesConfiguration.getFastTrackHrgKey())
            .setAmount(amount);

        return getRespond(builder);
    }

    public Fee getFeeForHearingMultiClaims(BigDecimal amount) {
        FeesApiRequest builder = new FeesApiRequest()
            .setChannel(feesConfiguration.getChannel())
            .setEvent(feesConfiguration.getHearingEvent())
            .setJurisdiction(feesConfiguration.getJurisdiction1())
            .setJurisdiction2(feesConfiguration.getJurisdiction2Hearing())
            .setService(feesConfiguration.getService())
            .setKeyword(feesConfiguration.getMultiClaimKey())
            .setAmount(amount);

        return getRespond(builder);
    }

    private Fee getRespond(FeesApiRequest feesApiRequest) {
        FeeLookupResponseDto feeLookupResponseDto;

        try {
            feeLookupResponseDto = feesApiClient.lookupFeeWithAmount(
                feesApiRequest.getService(),
                feesApiRequest.getJurisdiction(),
                feesApiRequest.getJurisdiction2(),
                feesApiRequest.getChannel(),
                feesApiRequest.getEvent(),
                feesApiRequest.getKeyword(),
                feesApiRequest.getAmount()
            );
        } catch (RestClientException e) {
            log.error("Fee Service Lookup Failed for [{}]", feesApiRequest);
            throw e;
        }
        if (isNull(feeLookupResponseDto) || isNull(feeLookupResponseDto.getFeeAmount())) {
            log.error("No Fees returned for [{}].", feesApiRequest);
            throw new InternalServerErrorException("No Fees returned by fee-service while creating hearing fee");
        }
        return buildFeeDto(feeLookupResponseDto);
    }

    private Fee buildFeeDto(FeeLookupResponseDto feeLookupResponseDto) {
        BigDecimal calculatedAmount = feeLookupResponseDto.getFeeAmount()
            .multiply(PENCE_PER_POUND)
            .setScale(0, RoundingMode.UNNECESSARY);

        return new Fee()
            .setCalculatedAmountInPence(calculatedAmount)
            .setCode(feeLookupResponseDto.getCode())
            .setVersion(feeLookupResponseDto.getVersion().toString())
            ;
    }
}
