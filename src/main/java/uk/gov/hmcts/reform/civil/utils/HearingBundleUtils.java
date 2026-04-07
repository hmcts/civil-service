package uk.gov.hmcts.reform.civil.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class HearingBundleUtils {

    public static final String DOCUMENTS = "DOCUMENTS";
    public static final String ELECTRONIC = "ELECTRONIC";
    public static final String SUMMARY = "SUMMARY";

    private static final Map<String, String> LABELS = Map.of(
        DOCUMENTS, "an indexed bundle of documents, with each page clearly numbered",
        ELECTRONIC, "an electronic bundle of digital documents",
        SUMMARY, "a case summary containing no more than 500 words"
    );

    private HearingBundleUtils() {
    }

    public static String buildBundleTypeText(List<String> types) {
        if (types == null || types.isEmpty()) {
            return "";
        }

        return types.stream()
            .map(HearingBundleUtils::resolveLabel)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" / "));
    }

    private static String resolveLabel(String type) {
        if (type == null || type.isBlank()) {
            return null;
        }
        return LABELS.getOrDefault(type, type);
    }
}
