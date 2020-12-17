package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;
import uk.gov.hmcts.reform.unspec.config.FeesConfiguration;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;
import uk.gov.hmcts.reform.unspec.model.Fee;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class FeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);

    private final FeesClient feesClient;
    private final FeesConfiguration feesConfiguration;

    public Fee getFeeDataByClaimValue(ClaimValue claimValue) {
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
