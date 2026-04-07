package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BundleTypeTextUtils {

    private BundleTypeTextUtils() {
        //NO-OP
    }

    public static String buildBundleTypeText(List<String> bundleTypes) {
        if (bundleTypes == null || bundleTypes.isEmpty()) {
            return "";
        }

        return bundleTypes.stream()
            .filter(Objects::nonNull)
            .map(FastTrackTrialBundleType::valueOf)
            .map(FastTrackTrialBundleType::getLabel)
            .collect(Collectors.joining(" / "));
    }
}
