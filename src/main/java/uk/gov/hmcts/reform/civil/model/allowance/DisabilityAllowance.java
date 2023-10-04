package uk.gov.hmcts.reform.civil.model.allowance;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Predicate;

@Getter
public enum DisabilityAllowance {

    SINGLE(139.75, DisabilityParam::disabledSingle),
    COUPLE(199.12, DisabilityParam::disabledCouple),
    SEVERE_DISABILITY_SINGLE(268.01,
                             DisabilityParam::disabledSeverelySingle
    ),
    SEVERE_DISABILITY_COUPLE(536.03,
                             DisabilityParam::disabledSeverelyCouple
    ),
    DISABLED_DEPENDANT(260.26, DisabilityParam::disabledDependant),
    CARER(149.93, DisabilityParam::isCarer),
    NO_DISABILITY(0.0, null);

    private double allowance;
    private final Predicate<DisabilityParam> disabilityMatcher;

    DisabilityAllowance(double allowance, Predicate<DisabilityParam> disabilityMatcher) {
        this.allowance = allowance;
        this.disabilityMatcher = disabilityMatcher;
    }

    public static double getDisabilityAllowance(DisabilityParam disabilityParam) {
        return Arrays.stream(DisabilityAllowance.values())
            .filter(disabilityAllowance -> disabilityAllowance.disabilityMatcher.test(disabilityParam))
            .mapToDouble(DisabilityAllowance ::getAllowance).sum();
    }


}
