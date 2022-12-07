package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.FeesConfiguration;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.fees.client.FeesClient;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;
import uk.gov.hmcts.reform.fees.client.model.FeeLookupResponseDto;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class FeesService {

    private static final BigDecimal PENCE_PER_POUND = BigDecimal.valueOf(100);

    private final FeesClient feesClient;
    private final FeesConfiguration feesConfiguration;

    public Fee getFeeDataByClaimValue(ClaimValue claimValue, boolean isSpecified) {
        FeeLookupResponseDto feeLookupResponseDto = lookupFee(claimValue, isSpecified);

        return buildFeeDto(feeLookupResponseDto);
    }

    private FeeLookupResponseDto lookupFee(ClaimValue claimValue, Boolean isSpecified) {
        return feesClient.lookupFee(
            feesConfiguration.getChannel(),
            feesConfiguration.getEvent(),
            claimValue.toPounds(),
            isSpecified
        );
    }

    // WARNING! the following function buildFeeDto is being used by both damages and specified claims,
    // any changes to the below code may break the code, please check with respective teams before making changes
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

    //calculate fee for specified claim total amount
    public Fee getFeeDataByTotalClaimAmount(BigDecimal totalClaimAmount) {
        FeeLookupResponseDto feeLookupResponseDto = specLookupFee(totalClaimAmount);

        return buildFeeDto(feeLookupResponseDto);
    }

    //lookup fee for specified claim total amount
    private FeeLookupResponseDto specLookupFee(BigDecimal totalClaimAmount) {
        return feesClient.lookupFee(
            feesConfiguration.getChannel(),
            feesConfiguration.getEvent(),
            totalClaimAmount.setScale(2),
            true
        );
    }

    public Fee getHearingFeeDataByTotalClaimAmount(BigDecimal totalClaimAmount) {
        FeeLookupResponseDto feeLookupResponseDto = specLookupHearingFee(totalClaimAmount);
        return buildFeeDto(feeLookupResponseDto);
    }

    private FeeLookupResponseDto specLookupHearingFee(BigDecimal totalClaimAmount) {
        return feesClient.lookupFee(
            feesConfiguration.getChannel(),
            feesConfiguration.getHearingEvent(),
            totalClaimAmount.setScale(2),
            true
        );
    }

    /**
     * Get a range of fees for the configured channel and event.
     *
     * @return an array containing a range of claim amounts with a fee for that range.
     */
    public Fee2Dto[] getFeeRange() {
        return feesClient.findRangeGroup(feesConfiguration.getChannel(), feesConfiguration.getEvent());
    }

}
