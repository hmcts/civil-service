package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.unspec.config.FeesConfiguration;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class FeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);

    private final FeesClient feesClient;
    private final FeesConfiguration feesConfiguration;

    public BigInteger getFeeAmountByClaimValue(ClaimValue claimValue) {
        FeeLookupResponseDto feeLookupResponseDto = lookupFee(claimValue);

        return getFeeAmountInPence(feeLookupResponseDto);
    }

    public FeeDto getFeeDataByClaimValue(ClaimValue claimValue) {
        FeeLookupResponseDto feeLookupResponseDto = lookupFee(claimValue);

        return buildFeeDto(feeLookupResponseDto);
    }

    private FeeLookupResponseDto lookupFee(ClaimValue claimValue) {
        return feesClient.lookupFee(
            feesConfiguration.getChannel(),
            feesConfiguration.getEvent(),
            claimValue.toPounds()
        );
    }

    private BigInteger getFeeAmountInPence(FeeLookupResponseDto feeLookupResponseDto) {
        var feeAmountPounds = feeLookupResponseDto.getFeeAmount();

        return feeAmountPounds.multiply(PENCE_PER_POUND).toBigInteger();
    }

    private FeeDto buildFeeDto(FeeLookupResponseDto feeLookupResponseDto) {
        return FeeDto.builder()
            .calculatedAmount(feeLookupResponseDto.getFeeAmount())
            .code(feeLookupResponseDto.getCode())
            .version(feeLookupResponseDto.getVersion().toString())
            .build();
    }
}
