package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.Optional;


public class StringUtil {

    public static boolean isEqualTo(YesOrNo value, YesOrNo compareTo) {
        return Optional.ofNullable(value)
            .map(compareTo::equals)
            .orElse(false);
    }
}
