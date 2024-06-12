package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.HearingFeeConfiguration;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.client.FeesApi;
import uk.gov.hmcts.reform.civil.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingFeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    private final FeesApi feesApi;
    private final HearingFeeConfiguration feesConfiguration;

    public Fee getFeeForHearingSmallClaims(BigDecimal amount) {

        FeeLookupResponseDto feeDto = feesApi.lookupFeeWithAmount(
            feesConfiguration.getService(),
            feesConfiguration.getJurisdiction1(),
            feesConfiguration.getJurisdiction2(),
            feesConfiguration.getChannel(),
            feesConfiguration.getHearingEvent(),
            feesConfiguration.getSmallClaimHrgKey(),
            amount
        );

        return buildFeeDto(feeDto);
    }

    public Fee getFeeForHearingFastTrackClaims(BigDecimal amount) {

        FeeLookupResponseDto feeDto = feesApi.lookupFeeWithAmount(
            feesConfiguration.getService(),
            feesConfiguration.getJurisdiction1(),
            feesConfiguration.getJurisdiction2Hearing(),
            feesConfiguration.getChannel(),
            feesConfiguration.getHearingEvent(),
            feesConfiguration.getFastTrackHrgKey(),
            amount
        );

        return buildFeeDto(feeDto);
    }

    public Fee getFeeForHearingMultiClaims(BigDecimal amount) {

        FeeLookupResponseDto feeDto = feesApi.lookupFeeWithAmount(
            feesConfiguration.getService(),
            feesConfiguration.getJurisdiction1(),
            feesConfiguration.getJurisdiction2Hearing(),
            feesConfiguration.getChannel(),
            feesConfiguration.getHearingEvent(),
            feesConfiguration.getMultiClaimKey(),
            amount
        );

        return buildFeeDto(feeDto);
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
