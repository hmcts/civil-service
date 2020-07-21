package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.payments.client.models.FeeDto;
import uk.gov.hmcts.reform.unspec.config.FeesConfiguration;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);
    private static final int ROUNDING_SCALE = 2;

    private final FeesClient feesClient;
    private final FeesConfiguration feesConfiguration;

    public BigInteger getFeeAmountByClaimValue(ClaimValue claimValue) {
        FeeLookupResponseDto feeLookupResponseDto = lookupFee(claimValue.getHigherValue());

        return getFeeAmountInPence(feeLookupResponseDto);
    }

    public FeeDto getFeeDataByClaimValue(ClaimValue claimValue) {
        FeeLookupResponseDto feeLookupResponseDto = lookupFee(claimValue.getHigherValue());

        return buildFeeDto(feeLookupResponseDto);
    }

    private FeeLookupResponseDto lookupFee(BigDecimal claimHigherValue) {
        var claimHigherValuePounds = convertToPounds(claimHigherValue);

        return feesClient.lookupFee(
            feesConfiguration.getChannel(),
            feesConfiguration.getEvent(),
            claimHigherValuePounds
        );
    }

    private BigDecimal convertToPounds(BigDecimal value) {
        return value.divide(PENCE_PER_POUND, ROUNDING_SCALE, RoundingMode.UNNECESSARY);
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
